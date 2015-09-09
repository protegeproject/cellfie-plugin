package org.mm.cellfie.ui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mm.cellfie.ui.exception.CellfieException;
import org.mm.core.MappingExpression;
import org.mm.parser.ParseException;
import org.mm.renderer.RendererException;
import org.mm.rendering.Rendering;
import org.mm.rendering.owlapi.OWLAPIRendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ss.SpreadSheetUtil;
import org.mm.ss.SpreadsheetLocation;
import org.mm.ui.DialogManager;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.ontology.OntologyPreferences;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

public class MapExpressionsAction implements ActionListener
{
	private ApplicationView container;

	private static final int CANCEL_IMPORT = 0;
	private static final int IMPORT_TO_NEW_ONTOLOGY = 1;
	private static final int IMPORT_TO_CURRENT_ONTOLOGY = 2;

	public MapExpressionsAction(ApplicationView container)
	{
		this.container = container;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try {
			SpreadSheetDataSource dataSource = container.getLoadedSpreadSheet();
			Workbook workbook = dataSource.getWorkbook();
			
			List<MappingExpression> mappings = getUserExpressions();
			if (mappings.isEmpty()) {
				throw new CellfieException("No mappings defined");
			}
			
			// TODO: Move this business logic inside the renderer
			Set<Rendering> results = new HashSet<Rendering>();
			StringBuffer logMessage = new StringBuffer();
			for (MappingExpression mapping : mappings) {
				if (mapping.isActive()) {
					String sheetName = mapping.getSheetName();
					Sheet sheet = workbook.getSheet(sheetName);
					int startColumn = SpreadSheetUtil.columnName2Number(mapping.getStartColumn());
					int startRow = SpreadSheetUtil.row2Number(mapping.getStartRow());
					int endColumn = mapping.hasEndColumnWildcard()
							? sheet.getRow(startRow).getLastCellNum()
							: SpreadSheetUtil.columnName2Number(mapping.getEndColumn());
					int endRow = mapping.hasEndRowWildcard()
							? sheet.getLastRowNum()
							: SpreadSheetUtil.row2Number(mapping.getEndRow());

					if (startColumn > endColumn) {
						throw new CellfieException("start column after finish column in expression " + mapping);
					}
					if (startRow > endRow) {
						throw new CellfieException("start row after finish row in expression " + mapping);
					}
					SpreadsheetLocation endLocation = new SpreadsheetLocation(sheetName, endColumn, endRow);
					SpreadsheetLocation startLocation = new SpreadsheetLocation(sheetName, startColumn, startRow);
					SpreadsheetLocation currentLocation = new SpreadsheetLocation(sheetName, startColumn, startRow);

					dataSource.setCurrentLocation(currentLocation);

					do {
						evaluate(mapping, results, logMessage);
						if (currentLocation.equals(endLocation)) {
							break;
						}
						currentLocation = incrementLocation(currentLocation, startLocation, endLocation);
						dataSource.setCurrentLocation(currentLocation);
					} while (true);
				}
			}
			storeLogToFile(logMessage);
			showAxiomPreviewDialog(toAxioms(results));
		}
		catch (Exception ex) {
			getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
		}
	}

	private void storeLogToFile(StringBuffer logMessage) throws FileNotFoundException
	{
		File logFile = container.createLogFile();
		PrintWriter printer = new PrintWriter(logFile);
		printer.print(logMessage);
		printer.close();
	}

	private Set<OWLAxiom> toAxioms(Set<Rendering> results)
	{
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		for (Rendering rendering : results) {
			if (rendering instanceof OWLAPIRendering) {
				axiomSet.addAll(((OWLAPIRendering) rendering).getOWLAxioms());
			}
		}
		return axiomSet;
	}

	private void showAxiomPreviewDialog(Set<OWLAxiom> axioms) throws CellfieException
	{
		OWLModelManager modelManager = container.getEditorKit().getOWLModelManager();
		OWLOntology currentOntology = modelManager.getActiveOntology();
		
		int answer = showConfirmImportDialog(axioms);
		try {
			switch (answer) {
				case IMPORT_TO_CURRENT_ONTOLOGY:
					modelManager.applyChanges(addAxioms(currentOntology, axioms));
					break;
				case IMPORT_TO_NEW_ONTOLOGY:
					OWLOntologyID id = createOntologyID();
					OWLOntology newOntology = modelManager.createNewOntology(id, id.getDefaultDocumentIRI().toURI());
					modelManager.applyChanges(addImport(newOntology, currentOntology));
					modelManager.applyChanges(addAxioms(newOntology, axioms));
					break;
				default:
					// NO-OP
			}
		} catch (OWLOntologyCreationException e) {
			throw new CellfieException("Error while creating a new ontology: " + e.getMessage());
		}
	}

	private List<? extends OWLOntologyChange> addImport(OWLOntology newOntology, OWLOntology currentOntology)
	{
		OWLModelManager modelManager = container.getEditorKit().getOWLModelManager();
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		IRI currentOntologyIRI = currentOntology.getOntologyID().getOntologyIRI();
		OWLImportsDeclaration importDeclaration = modelManager.getOWLDataFactory().getOWLImportsDeclaration(currentOntologyIRI);
		changes.add(new AddImport(newOntology, importDeclaration));
		return changes;
	}

	private OWLOntologyID createOntologyID()
	{
		OntologyPreferences ontologyPreferences = OntologyPreferences.getInstance();
		IRI freshIRI = IRI.create(ontologyPreferences.generateNextURI());
		return new OWLOntologyID(freshIRI);
	}

	private List<OWLOntologyChange> addAxioms(OWLOntology ontology, Set<OWLAxiom> axioms)
	{
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLAxiom ax : axioms) {
			changes.add(new AddAxiom(ontology, ax));
		}
		return changes;
	}

	private JPanel createPreviewAxiomsPanel(Set<OWLAxiom> axioms)
	{
		return new PreviewAxiomsPanel(container, container.getEditorKit(), axioms);
	}

	private int showConfirmImportDialog(Set<OWLAxiom> axioms)
	{
		ImportOption[] options = {
				new ImportOption(CANCEL_IMPORT, "Cancel"),
				new ImportOption(IMPORT_TO_NEW_ONTOLOGY, "Import to new ontology"),
				new ImportOption(IMPORT_TO_CURRENT_ONTOLOGY, "Import to the current ontology")
		};
		return JOptionPaneEx.showConfirmDialog(container, "Import Axioms", createPreviewAxiomsPanel(axioms), JOptionPane.PLAIN_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, options, null);
	}

	private void evaluate(MappingExpression mapping, Set<Rendering> results, StringBuffer logMessage) throws ParseException
	{
		container.evaluate(mapping, container.getDefaultRenderer(), results, logMessage);
	}

	private SpreadsheetLocation incrementLocation(SpreadsheetLocation current, SpreadsheetLocation start, SpreadsheetLocation end)
			throws RendererException
	{
		if (current.getPhysicalRowNumber() < end.getPhysicalRowNumber()) {
			return new SpreadsheetLocation(current.getSheetName(), current.getPhysicalColumnNumber(), current.getPhysicalRowNumber() + 1);
		}
		if (current.getPhysicalRowNumber() == end.getPhysicalRowNumber()) {
			if (current.getPhysicalColumnNumber() < end.getPhysicalColumnNumber()) {
				return new SpreadsheetLocation(current.getSheetName(), current.getPhysicalColumnNumber() + 1, start.getPhysicalRowNumber());
			}
		}
		throw new RendererException("incrementLocation called redundantly");
	}

	private List<MappingExpression> getUserExpressions()
	{
		return container.getMappingBrowserView().getMappingExpressions();
	}

	private DialogManager getApplicationDialogManager()
	{
		return container.getApplicationDialogManager();
	}

	class ImportOption implements Comparable<ImportOption>
	{
		private int option;
		private String title;
		
		public ImportOption(int option, String title)
		{
			this.option = option;
			this.title = title;
		}
		
		public int get()
		{
			return option;
		}
		
		@Override
		public String toString()
		{
			return title;
		}

		@Override
		public int compareTo(ImportOption o)
		{
			return option-o.option;
		}
	}
}
