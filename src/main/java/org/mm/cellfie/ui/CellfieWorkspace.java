package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.mm.app.MMApplication;
import org.mm.app.MMApplicationModel;
import org.mm.cellfie.OWLProtegeOntology;
import org.mm.core.OWLOntologySource;
import org.mm.core.TransformationRule;
import org.mm.core.TransformationRuleSet;
import org.mm.core.settings.ReferenceSettings;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.SimpleNode;
import org.mm.parser.node.ExpressionNode;
import org.mm.parser.node.MMExpressionNode;
import org.mm.renderer.Renderer;
import org.mm.rendering.Rendering;
import org.mm.ss.SpreadSheetDataSource;
import org.protege.editor.core.ui.split.ViewSplitPane;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main Mapping Master user interface. It contains a view of a
 * spreadsheet and a control area to edit and execute Mapping Master rules.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class CellfieWorkspace extends JPanel {

   private static final Logger logger = LoggerFactory.getLogger(CellfieWorkspace.class);

   private static final long serialVersionUID = 1L;

   private final OWLOntologySource ontologySource;
   private final File workbookFile;
   private final OWLEditorKit editorKit;

   private final WorkbookView dataSourceView;
   private final TransformationRuleBrowserView ruleBrowserView;

   private File ruleFile;

   private MMApplicationModel applicationModel;

   public CellfieWorkspace(@Nonnull OWLOntologySource ontologySource, @Nonnull File workbookFile,
         @Nonnull OWLEditorKit editorKit) throws Exception {
      this.ontologySource = checkNotNull(ontologySource);
      this.workbookFile = checkNotNull(workbookFile);
      this.editorKit = checkNotNull(editorKit);

      applicationModel = MMApplication.create(ontologySource, workbookFile).getApplicationModel();

      setLayout(new BorderLayout());

      JPanel pnlTargetOntology = new JPanel(new FlowLayout(FlowLayout.LEFT));
      pnlTargetOntology.setBorder(new EmptyBorder(5, 5, 0, 5));
      add(pnlTargetOntology, BorderLayout.NORTH);

      JLabel lblTargetOntology = new JLabel("Target Ontology: ");
      lblTargetOntology.setForeground(Color.DARK_GRAY);
      pnlTargetOntology.add(lblTargetOntology);

      JLabel lblOntologyID = new JLabel(getTitle(ontologySource.getOWLOntology()));
      lblOntologyID.setForeground(Color.DARK_GRAY);
      pnlTargetOntology.add(lblOntologyID);

      ViewSplitPane splitPane = new ViewSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitPane.setResizeWeight(0.4);
      add(splitPane, BorderLayout.CENTER);

      /*
       * Workbook sheet GUI presentation
       */
      dataSourceView = new WorkbookView(this);
      splitPane.setTopComponent(dataSourceView);

      /*
       * Transformation rule browser, create, edit, remove panel
       */
      ruleBrowserView = new TransformationRuleBrowserView(this);
      splitPane.setBottomComponent(ruleBrowserView);

      validate();
   }

   public OWLEditorKit getOWLEditorKit() {
      return editorKit;
   }

   public File getWorkbookFile() {
      return workbookFile;
   }

   public Optional<File> getRuleFile() {
      return Optional.ofNullable(ruleFile);
   }

   public boolean isRuleFilePresent() {
      return getRuleFile().isPresent();
   }

   public void setRuleFile(@Nonnull File ruleFile) {
      this.ruleFile = checkNotNull(ruleFile);
      fireApplicationModelChanged();
   }

   private void fireApplicationModelChanged() {
      try {
         applicationModel = MMApplication.create(ontologySource, workbookFile, ruleFile).getApplicationModel();
      } catch (Exception e) {
         String message = "Unable to start Cellfie Workspace (see log for details)";
         DialogUtils.showErrorDialog(this, message);
         logger.error(message, e);
      }
   }

   public void evaluate(@Nonnull TransformationRule rule, @Nonnull Renderer renderer,
         @Nonnull Set<Rendering> results) throws ParseException {
      String ruleExpression = rule.getRuleExpression();
      MappingMasterParser parser = new MappingMasterParser(
            new ByteArrayInputStream(ruleExpression.getBytes()), new ReferenceSettings(), -1);
      SimpleNode simpleNode = parser.expression();
      MMExpressionNode ruleNode = new ExpressionNode((ASTExpression) simpleNode)
            .getMMExpressionNode();
      Optional<? extends Rendering> renderingResult = renderer.render(ruleNode);
      if (renderingResult.isPresent()) {
         results.add(renderingResult.get());
      }
   }

   public WorkbookView getDataSourceView() { // TODO: Rename to getWorkbookView
      return dataSourceView;
   }

   public TransformationRuleBrowserView getTransformationRuleBrowserView() { // TODO: Shorten to getRuleBrowserView
      return ruleBrowserView;
   }

   public OWLOntology getActiveOntology() { // TODO Rename to getOntology
      return ontologySource.getOWLOntology();
   }

   public SpreadSheetDataSource getActiveWorkbook() { // TODO Rename to getWorkbook
      return applicationModel.getWorkbook();
   }

   public List<TransformationRule> getActiveTransformationRules() { // TODO Rename to getTransformationRules
      return applicationModel.getTransformationRuleModel().getRules();
   }

   /* package */ void updateTransformationRuleModel() {
      final List<TransformationRule> rules = getTransformationRuleBrowserView().getPickedRules();
      TransformationRuleSet ruleSet = TransformationRuleSet.create(rules);
      applicationModel.getTransformationRuleModel().changeTransformationRuleSet(ruleSet);
   }

   public Renderer getDefaultRenderer() {
      return applicationModel.getTransformationRenderer();
   }

   public Renderer getLogRenderer() {
      return applicationModel.getLogRenderer();
   }

   public boolean shouldClose() {
      return ruleBrowserView.safeGuardChanges();
   }

   public static JDialog createDialog(@Nonnull JComponent parent, @Nonnull OWLEditorKit editorKit,
         @Nonnull File workbookFile) throws Exception {
      JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, parent);
      final JDialog dialog = new JDialog(parentFrame, "Cellfie", Dialog.ModalityType.MODELESS);

      final OWLOntologySource ontologySource = new OWLProtegeOntology(editorKit);
      final CellfieWorkspace workspacePanel = new CellfieWorkspace(ontologySource, workbookFile, editorKit);
      workspacePanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

      // Closing Cellfie using ESC key
      workspacePanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CLOSE_DIALOG");
      workspacePanel.getActionMap().put("CLOSE_DIALOG", new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) {
            int answer = DialogUtils.showConfirmDialog(dialog, "Exit Cellfie?");
            switch (answer) {
               case JOptionPane.YES_OPTION :
                  if (workspacePanel.shouldClose()) {
                     dialog.setVisible(false);
                  }
            }
         }
      });

      // Closing Cellfie using close [x] button 
      dialog.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            if (workspacePanel.shouldClose()) {
               dialog.setVisible(false);
            }
         }
      });

      dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      dialog.setContentPane(workspacePanel);
      dialog.setSize(1200, 900);
      dialog.setResizable(true);
      return dialog;
   }

   private static String getTitle(OWLOntology ontology) {
      if (ontology.getOntologyID().isAnonymous()) {
         return ontology.getOntologyID().toString();
      }
      final com.google.common.base.Optional<IRI> iri = ontology.getOntologyID().getDefaultDocumentIRI();
      return getOntologyLabelText(iri);
   }

   private static String getOntologyLabelText(com.google.common.base.Optional<IRI> iri) {
      StringBuilder sb = new StringBuilder();
      if (iri.isPresent()) {
         String shortForm = new OntologyIRIShortFormProvider().getShortForm(iri.get());
         sb.append(shortForm);
      } else {
         sb.append("Anonymous ontology");
      }
      sb.append(" (");
      if (iri.isPresent()) {
         sb.append(iri.get().toString());
      }
      sb.append(")");
      return sb.toString();
   }
}
