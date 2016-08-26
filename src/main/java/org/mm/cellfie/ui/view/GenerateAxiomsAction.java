package org.mm.cellfie.ui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.poi.ss.usermodel.Sheet;
import org.mm.cellfie.ui.exception.CellfieException;
import org.mm.cellfie.ui.view.WorkspacePanel.RenderLogging;
import org.mm.core.TransformationRule;
import org.mm.parser.ParseException;
import org.mm.renderer.RendererException;
import org.mm.rendering.Rendering;
import org.mm.rendering.owlapi.OWLRendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ss.SpreadSheetUtil;
import org.mm.ss.SpreadsheetLocation;
import org.mm.ui.DialogManager;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.OWLEditorKit;
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

import com.google.common.base.Optional;

public class GenerateAxiomsAction implements ActionListener
{
   private WorkspacePanel container;

   private OWLEditorKit editorKit;
   private OWLModelManager modelManager;

   private static final int CANCEL_IMPORT = 0;
   private static final int ADD_TO_NEW_ONTOLOGY = 1;
   private static final int ADD_TO_CURRENT_ONTOLOGY = 2;

   public GenerateAxiomsAction(WorkspacePanel container)
   {
      this.container = container;
      editorKit = container.getEditorKit();
      modelManager = container.getEditorKit().getModelManager();
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      try {
         // Get all user-defined transformation rules
         List<TransformationRule> rules = getUserRules();
         
         // Initialize Cellfie logging
         initLogging();

         // TODO: Move this business logic inside the renderer
         Set<Rendering> results = new HashSet<Rendering>();
         for (TransformationRule rule : rules) {
            if (rule.isActive()) {
               String sheetName = rule.getSheetName();
               Sheet sheet = getActiveWorkbook().getWorkbook().getSheet(sheetName);
               
               int startColumnIndex = getStartColumnIndex(rule);
               int startRowIndex = getStartRowIndex(rule);
               int endColumnIndex = getEndColumnIndex(rule, sheet, startRowIndex);
               int endRowIndex = getEndRowIndex(rule, sheet);

               if (startColumnIndex > endColumnIndex) {
                  throw new CellfieException("Start column after finish column in rule " + rule);
               }
               if (startRowIndex > endRowIndex) {
                  throw new CellfieException("Start row after finish row in rule " + rule);
               }

               SpreadsheetLocation endLocation = new SpreadsheetLocation(sheetName, endColumnIndex, endRowIndex);
               SpreadsheetLocation startLocation = new SpreadsheetLocation(sheetName, startColumnIndex, startRowIndex);
               SpreadsheetLocation currentLocation = new SpreadsheetLocation(sheetName, startColumnIndex, startRowIndex);

               getActiveWorkbook().setCurrentLocation(currentLocation);
               do {
                  evaluate(rule, results, container.getRenderLogging());
                  if (currentLocation.equals(endLocation)) {
                     break;
                  }
                  currentLocation = incrementLocation(currentLocation, startLocation, endLocation);
                  getActiveWorkbook().setCurrentLocation(currentLocation);
               } while (true);
            }
         }
         // Store Cellfie logging to a file
         saveLogging();
         
         // Show the preview dialog to users to see all the generated axioms
         showAxiomPreviewDialog(toAxioms(results));
      } catch (Exception ex) {
         getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
      }
   }

   private int getStartColumnIndex(TransformationRule rule) throws Exception
   {
      String startColumn = rule.getStartColumn();
      if (startColumn.isEmpty()) {
         throw new CellfieException("Start column is not specified");
      }
      return SpreadSheetUtil.columnName2Number(startColumn);
   }

   private int getStartRowIndex(TransformationRule rule) throws Exception
   {
      String startRow = rule.getStartRow();
      if (startRow.isEmpty()) {
         throw new CellfieException("Start row is not specified");
      }
      return SpreadSheetUtil.rowLabel2Number(startRow);
   }

   private int getEndColumnIndex(TransformationRule rule, Sheet sheet, int startRowIndex) throws Exception
   {
      String endColumn = rule.getEndColumn();
      if (endColumn.isEmpty()) {
         throw new CellfieException("End column is not specified. (Hint: Use a wildcard '+' to indicate the last column)");
      }
      return rule.hasEndColumnWildcard()
            ? sheet.getRow(startRowIndex).getLastCellNum() + 1
            : SpreadSheetUtil.columnName2Number(endColumn);
   }

   private int getEndRowIndex(TransformationRule rule, Sheet sheet) throws Exception
   {
      String endRow = rule.getEndRow();
      if (endRow.isEmpty()) {
         throw new CellfieException("End row is not specified. (Hint: Use a wildcard '+' to indicate the last row)");
      }
      int endRowIndex = rule.hasEndRowWildcard() ? sheet.getLastRowNum() + 1
            : SpreadSheetUtil.rowLabel2Number(endRow);
      return endRowIndex;
   }

   private Set<OWLAxiom> toAxioms(Set<Rendering> results)
   {
      Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
      for (Rendering rendering : results) {
         if (rendering instanceof OWLRendering) {
            axiomSet.addAll(((OWLRendering) rendering).getOWLAxioms());
         }
      }
      return axiomSet;
   }

   private void showAxiomPreviewDialog(Set<OWLAxiom> axioms) throws CellfieException
   {
      final ImportOption[] options = { new ImportOption(CANCEL_IMPORT, "Cancel"),
            new ImportOption(ADD_TO_NEW_ONTOLOGY, "Add to a new ontology"),
            new ImportOption(ADD_TO_CURRENT_ONTOLOGY, "Add to current ontology") };
      try {
         OWLOntology currentOntology = container.getActiveOntology();
         int answer = JOptionPaneEx.showConfirmDialog(container, "Generated Axioms", createPreviewAxiomsPanel(axioms),
               JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[1]);
         switch (answer) {
            case ADD_TO_CURRENT_ONTOLOGY :
               modelManager.applyChanges(addAxioms(currentOntology, axioms));
               break;
            case ADD_TO_NEW_ONTOLOGY :
               OWLOntologyID id = createOntologyID();
               OWLOntology newOntology = modelManager.createNewOntology(id, id.getDefaultDocumentIRI().get().toURI());
               modelManager.applyChanges(addImport(newOntology, currentOntology));
               modelManager.applyChanges(addAxioms(newOntology, axioms));
               break;
         }
      } catch (ClassCastException e) {
         // NO-OP: Fix should be to Protege API
      } catch (OWLOntologyCreationException e) {
         throw new CellfieException("Error while creating a new ontology: " + e.getMessage());
      }
   }

   private List<? extends OWLOntologyChange> addImport(OWLOntology newOntology, OWLOntology currentOntology)
   {
      List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
      Optional<IRI> ontologyIri = currentOntology.getOntologyID().getOntologyIRI();
      if (ontologyIri.isPresent()) {
         OWLImportsDeclaration importDeclaration = modelManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri.get());
         changes.add(new AddImport(newOntology, importDeclaration));
      }
      return changes;
   }

   private OWLOntologyID createOntologyID()
   {
      OntologyPreferences ontologyPreferences = OntologyPreferences.getInstance();
      IRI freshIRI = IRI.create(ontologyPreferences.generateNextURI());
      return new OWLOntologyID(Optional.of(freshIRI), Optional.absent());
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
      return new PreviewAxiomsPanel(container, editorKit, axioms);
   }

   private void evaluate(TransformationRule rule, Set<Rendering> results, RenderLogging logging) throws ParseException
   {
      container.evaluate(rule, container.getDefaultRenderer(), results);
      container.log(rule, container.getLogRenderer(), logging);
   }

   private SpreadsheetLocation incrementLocation(SpreadsheetLocation current, SpreadsheetLocation start,
         SpreadsheetLocation end) throws RendererException
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

   private SpreadSheetDataSource getActiveWorkbook() throws CellfieException
   {
      SpreadSheetDataSource dataSource = container.getActiveWorkbook();
      if (dataSource == null) {
         throw new CellfieException("No workbook was loaded");
      }
      return dataSource;
   }
   
   private List<TransformationRule> getUserRules() throws CellfieException
   {
      List<TransformationRule> rules = container.getTransformationRuleBrowserView().getTransformationRules();
      if (rules.isEmpty()) {
         throw new CellfieException("No transformation rules were created");
      }
      return rules;
   }

   private DialogManager getApplicationDialogManager()
   {
      return container.getApplicationDialogManager();
   }

   private void initLogging()
   {
      container.initLogging();
   }

   private void saveLogging() throws FileNotFoundException
   {
      container.getRenderLogging().save();
   }

   /**
    * A helper class for creating import axioms command buttons.
    */
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
         return option - o.option;
      }
   }
}
