package org.mm.cellfie.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.ProtocolException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mm.cellfie.ui.dialog.MMDialogManager;
import org.mm.cellfie.ui.model.DataSourceModel;
import org.mm.cellfie.ui.view.ApplicationView;
import org.mm.core.MappingExpression;
import org.mm.exceptions.MappingMasterException;
import org.mm.parser.ParseException;
import org.mm.renderer.RendererException;
import org.mm.rendering.Rendering;
import org.mm.rendering.owlapi.OWLAPIRendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ss.SpreadSheetUtil;
import org.mm.ss.SpreadsheetLocation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class MapExpressionsAction implements ActionListener
{
	private ApplicationView container;

	private static final int IMPORT_TO_CURRENT_ONTOLOGY = 1;
	private static final int IMPORT_TO_NEW_ONTOLOGY = 2;

	public MapExpressionsAction(ApplicationView container)
	{
		this.container = container;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try {
			/*
			 * Verify the input resources first
			 */
			verify();
			
			// TODO: Move this business logic inside the renderer
			Set<Rendering> results = new HashSet<Rendering>();
			List<MappingExpression> mappings = getMappingExpressions();
			SpreadSheetDataSource dataSource = getDataSourceModel().getDataSource();
			Workbook workbook = dataSource.getWorkbook();
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
						throw new RendererException("start column after finish column in expression " + mapping);
					}
					if (startRow > endRow) {
						throw new RendererException("start row after finish row in expression " + mapping);
					}
					SpreadsheetLocation endLocation = new SpreadsheetLocation(sheetName, endColumn, endRow);
					SpreadsheetLocation startLocation = new SpreadsheetLocation(sheetName, startColumn, startRow);
					SpreadsheetLocation currentLocation = new SpreadsheetLocation(sheetName, startColumn, startRow);

					dataSource.setCurrentLocation(currentLocation);

					do {
						evaluate(mapping, results);
						if (currentLocation.equals(endLocation)) {
							break;
						}
						currentLocation = incrementLocation(currentLocation, startLocation, endLocation);
						dataSource.setCurrentLocation(currentLocation);
					} while (true);
				}
			}
			confirmImport(results);
		}
		catch (Exception ex) {
			getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
		}
	}

	private void confirmImport(Set<Rendering> results) throws MappingMasterException
	{
		int answer = showConfirmImportDialog();
		try {
			switch (answer) {
				case IMPORT_TO_CURRENT_ONTOLOGY:
					OWLOntology currentOntology = container.getApplicationModel().getOntology();
					importResult(currentOntology, results);
					break;
				case IMPORT_TO_NEW_ONTOLOGY:
					File file = container.getApplicationDialogManager().showSaveFileChooser(container, "Save", "owl", "OWL ontology file", true);
					if (file != null) {
						OWLOntology newOntology = OWLManager.createOWLOntologyManager().createOntology(IRI.create(file.toURI()));
						importResult(newOntology, results);
					}
					break;
				default:
					// NO-OP
			}
		} catch (OWLOntologyCreationException e) {
			throw new MappingMasterException("Error while creating a new ontology file: " + e.getMessage());
		} catch (OWLOntologyStorageException e) {
			if (e.getCause() instanceof ProtocolException) {
				throw new MappingMasterException("Unable to import the axioms to remote location. Please make sure your ontology was loaded from a local directory.");
			}
			throw new MappingMasterException("Error while importing the axioms to target ontology: " + e.getMessage());
		}
	}

	private void importResult(OWLOntology ontology, Set<Rendering> results) throws OWLOntologyStorageException
	{
		int counter = 0;
		for (Rendering rendering : results) {
			if (rendering instanceof OWLAPIRendering) {
				Set<OWLAxiom> owlAxioms = ((OWLAPIRendering) rendering).getOWLAxioms();
				for (OWLAxiom axiom : owlAxioms) {
					List<OWLOntologyChange> changes = ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
					counter += changes.size();
				}
			}
		}
		IRI ontologyIri = ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology);
		ontology.getOWLOntologyManager().saveOntology(ontology, ontologyIri);
		getApplicationDialogManager().showMessageDialog(container, "Cellfie successfully imports " + counter + " axioms to ontology " + ontologyIri);
	}

	private int showConfirmImportDialog()
	{
		ImportOption[] options = {
				new ImportOption(IMPORT_TO_CURRENT_ONTOLOGY, "Import axioms to the current ontology"),
				new ImportOption(IMPORT_TO_NEW_ONTOLOGY, "Import axioms to new ontology")
		};
		Object answer = JOptionPane.showInputDialog(container, "Please select the import action:", "Import", JOptionPane.DEFAULT_OPTION, null, options, null);
		if (answer != null) {
			return ((ImportOption) answer).get();
		}
		return 0;
	}

	private void verify() throws MappingMasterException
	{
		if (getMappingExpressions().isEmpty()) {
			throw new MappingMasterException("No mappings defined");
		}
		if (getDataSourceModel().isEmpty()) {
			throw new MappingMasterException("No workbook loaded");
		}
	}

	private void evaluate(MappingExpression mapping, Set<Rendering> results) throws ParseException
	{
		container.evaluate(mapping, container.getDefaultRenderer(), results);
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

	private List<MappingExpression> getMappingExpressions()
	{
		return container.getMappingBrowserView().getMappingExpressions();
	}

	private DataSourceModel getDataSourceModel()
	{
		return container.getApplicationModel().getDataSourceModel();
	}

	private MMDialogManager getApplicationDialogManager()
	{
		return container.getApplicationDialogManager();
	}

	class ImportOption
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
	}
}
