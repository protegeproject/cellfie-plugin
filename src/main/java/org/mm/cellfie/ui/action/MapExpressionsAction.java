package org.mm.cellfie.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mm.cellfie.ui.view.ApplicationView;
import org.mm.cellfie.ui.view.PreviewAxiomsPanel;
import org.mm.core.MappingExpression;
import org.mm.exceptions.MappingMasterException;
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
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

public class MapExpressionsAction implements ActionListener
{
	private ApplicationView container;

	private static final int CANCEL_IMPORT = 0;
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
			SpreadSheetDataSource dataSource = container.getLoadedSpreadSheet();
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
			showAxiomPreviewDialog(toAxioms(results));
		}
		catch (Exception ex) {
			getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
		}
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

	private void showAxiomPreviewDialog(Set<OWLAxiom> axioms) throws MappingMasterException
	{
		OWLModelManager modelManager = container.getEditorKit().getOWLModelManager();
		
		int answer = showConfirmImportDialog(axioms);
		try {
			switch (answer) {
				case IMPORT_TO_CURRENT_ONTOLOGY:
					OWLOntology currentOntology = modelManager.getActiveOntology();
					modelManager.applyChanges(addAxioms(currentOntology, axioms));
					break;
				case IMPORT_TO_NEW_ONTOLOGY:
					OWLOntology newOntology = modelManager.createNewOntology(new OWLOntologyID(), null);
					modelManager.applyChanges(addAxioms(newOntology, axioms));
					break;
				default:
					// NO-OP
			}
		} catch (OWLOntologyCreationException e) {
			throw new MappingMasterException("Error while creating a new ontology: " + e.getMessage());
		}
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
		return new PreviewAxiomsPanel(container.getEditorKit(), axioms);
	}

	private int showConfirmImportDialog(Set<OWLAxiom> axioms)
	{
		ImportOption[] options = {
				new ImportOption(CANCEL_IMPORT, "Don't Import"),
				new ImportOption(IMPORT_TO_CURRENT_ONTOLOGY, "Import axioms to the current ontology"),
				new ImportOption(IMPORT_TO_NEW_ONTOLOGY, "Import axioms to new ontology")
		};
		return JOptionPaneEx.showConfirmDialog(container, "Import", createPreviewAxiomsPanel(axioms), JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, options, null);
	}

	private void verify() throws MappingMasterException
	{
		if (getMappingExpressions().isEmpty()) {
			throw new MappingMasterException("No mappings defined");
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
