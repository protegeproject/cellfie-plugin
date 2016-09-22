package org.mm.cellfie.ui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.poi.ss.usermodel.Sheet;
import org.mm.cellfie.ui.exception.CellfieException;
import org.mm.core.TransformationRule;
import org.mm.core.settings.ReferenceSettings;
import org.mm.core.settings.ValueEncodingSetting;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.node.ExpressionNode;
import org.mm.parser.node.MMExpressionNode;
import org.mm.renderer.RendererException;
import org.mm.rendering.Rendering;
import org.mm.rendering.owlapi.OWLRendering;
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

public class GenerateAxiomsAction implements ActionListener
{
   private static final int CANCEL_IMPORT = 0;
   private static final int ADD_TO_NEW_ONTOLOGY = 1;
   private static final int ADD_TO_CURRENT_ONTOLOGY = 2;

   private final WorkspacePanel container;
   private final OWLModelManager modelManager;

   public GenerateAxiomsAction(WorkspacePanel container)
   {
      this.container = container;
      modelManager = container.getEditorKit().getModelManager();
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      try {
         // Get all user-defined transformation rules
         List<TransformationRule> rules = getUserRules();
         
         // Initialize string builder to stack log messages
         StringBuilder logBuilder = new StringBuilder(getLogHeader());

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
               logExpression(rule, logBuilder);
               do {
                  evaluate(rule, results);
                  logEvaluation(rule, logBuilder);
                  if (currentLocation.equals(endLocation)) {
                     break;
                  }
                  currentLocation = incrementLocation(currentLocation, startLocation, endLocation);
                  getActiveWorkbook().setCurrentLocation(currentLocation);
               } while (true);
            }
         }
         String logMessage = logBuilder.toString();
         
         // Store Cellfie logging to a file
         LogUtils.save(getLoggingFile(), logMessage, true);
         
         // Show the preview dialog to users to see all the generated axioms
         showAxiomPreviewDialog(toAxioms(results), logMessage);
      }
      catch (Exception ex) {
         getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
      }
   }

   private void logExpression(TransformationRule rule, StringBuilder logBuilder)
   {
       logBuilder.append("\n");
       String additionalInformation = String.format("Cell range: (%s!%s%s:%s%s) Comment: \"%s\"",
               rule.getSheetName(), rule.getStartColumn(), rule.getStartRow(), rule.getEndColumn(), rule.getEndRow(),
               rule.getComment());
       logBuilder.append(asComment(additionalInformation));
       logBuilder.append("\n");
       logBuilder.append(asComment(rule.getRuleString()));
       logBuilder.append("\n\n");
   }

   private static String asComment(String text)
   {
      return text.replaceAll("(?m)^(.*)", "# $1");
   }

   private File getLoggingFile()
   {
      String rootDir = getDefaultRootDirectory();
      Optional<String> ruleFilePath = container.getRuleFileLocation();
      if (ruleFilePath.isPresent()) {
         rootDir = new File(ruleFilePath.get()).getParent();
      }
      if (!rootDir.endsWith(System.getProperty("file.separator"))) {
         rootDir += System.getProperty("file.separator");
      }
      return new File(rootDir, "cellfie.log");
   }

   private String getLogHeader()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("Date: ").append(LogUtils.getTimestamp());
      sb.append("\n");
      sb.append("Ontology source: ").append(container.getOntologyFileLocation());
      sb.append("\n");
      sb.append("Worksheet source: ").append(container.getWorkbookFileLocation());
      sb.append("\n");
      sb.append("Transformation rules: ").append(container.getRuleFileLocation().isPresent() ? container.getRuleFileLocation().get() : "N/A");
      sb.append("\n");
      return sb.toString();
   }

   private String getDefaultRootDirectory()
   {
      return System.getProperty("java.io.tmpdir");
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

   private void showAxiomPreviewDialog(Set<OWLAxiom> axioms, String logMessage) throws CellfieException
   {
      final ImportOption[] options = { new ImportOption(CANCEL_IMPORT, "Cancel"),
            new ImportOption(ADD_TO_NEW_ONTOLOGY, "Add to a new ontology"),
            new ImportOption(ADD_TO_CURRENT_ONTOLOGY, "Add to current ontology") };
      try {
         OWLOntology currentOntology = container.getActiveOntology();
         int answer = JOptionPaneEx.showConfirmDialog(container, "Generated Axioms", createPreviewAxiomsPanel(axioms, logMessage),
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
      com.google.common.base.Optional<IRI> ontologyIri = currentOntology.getOntologyID().getOntologyIRI();
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
      return new OWLOntologyID(com.google.common.base.Optional.of(freshIRI), com.google.common.base.Optional.absent());
   }

   private List<OWLOntologyChange> addAxioms(OWLOntology ontology, Set<OWLAxiom> axioms)
   {
      List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
      for (OWLAxiom ax : axioms) {
         changes.add(new AddAxiom(ontology, ax));
      }
      return changes;
   }

   private JPanel createPreviewAxiomsPanel(Set<OWLAxiom> axioms, String logMessage)
   {
      return new PreviewAxiomsPanel(container, axioms, logMessage);
   }

   private void evaluate(TransformationRule rule, Set<Rendering> results) throws ParseException
   {
      container.evaluate(rule, container.getDefaultRenderer(), results);
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
      List<TransformationRule> rules = container.getTransformationRuleBrowserView().getSelectedRules();
      if (rules.isEmpty()) {
         throw new CellfieException("No transformation rules were selected");
      }
      return rules;
   }

   private DialogManager getApplicationDialogManager()
   {
      return container.getApplicationDialogManager();
   }

   private void logEvaluation(TransformationRule rule, StringBuilder logBuilder) throws ParseException
   {
      String ruleString = rule.getRuleString();
      MappingMasterParser parser = new MappingMasterParser(new ByteArrayInputStream(ruleString.getBytes()), getReferenceSettings(), -1);
      MMExpressionNode ruleNode = new ExpressionNode((ASTExpression) parser.expression()).getMMExpressionNode();
      Optional<? extends Rendering> renderingResult = container.getLogRenderer().render(ruleNode);
      if (renderingResult.isPresent()) {
         logBuilder.append(renderingResult.get().getRendering());
      }
   }

   private ReferenceSettings getReferenceSettings()
   {
      ReferenceSettings referenceSettings = new ReferenceSettings();
      referenceSettings.setValueEncodingSetting(ValueEncodingSetting.RDFS_LABEL);
      return referenceSettings;
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
