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
import java.io.File;
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
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mm.cellfie.ProtegeEntityResolver;
import org.mm.cellfie.transformationrule.TransformationRule;
import org.mm.cellfie.transformationrule.TransformationRuleSet;
import org.mm.parser.ParseException;
import org.mm.renderer.RenderingContext;
import org.mm.renderer.Workbook;
import org.mm.renderer.owl.OwlEntityResolver;
import org.mm.renderer.owl.OwlFactory;
import org.mm.renderer.owl.OwlRenderer;
import org.protege.editor.core.ui.split.ViewSplitPane;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import com.google.common.collect.Sets;

/**
 * This is the main Mapping Master user interface. It contains a view of a
 * spreadsheet and a control area to edit and execute Mapping Master rules.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class CellfieWorkspace extends JPanel {

   private static final long serialVersionUID = 1L;

   private final OWLOntology ontology;
   private final Workbook workbook;
   private final OwlRenderer renderer;
   private final OWLEditorKit editorKit;

   private final WorkbookView workbookView;
   private final TransformationRuleBrowserView ruleBrowserView;

   public CellfieWorkspace(@Nonnull OWLOntology ontology, @Nonnull Workbook workbook,
         @Nonnull OwlRenderer renderer, @Nonnull OWLEditorKit editorKit) {
      this.ontology = checkNotNull(ontology);
      this.workbook = checkNotNull(workbook);
      this.renderer = checkNotNull(renderer);
      this.editorKit = checkNotNull(editorKit);

      setLayout(new BorderLayout());

      JPanel pnlTargetOntology = new JPanel(new FlowLayout(FlowLayout.LEFT));
      pnlTargetOntology.setBorder(new EmptyBorder(5, 5, 0, 5));
      add(pnlTargetOntology, BorderLayout.NORTH);

      JLabel lblTargetOntology = new JLabel("Target Ontology: ");
      lblTargetOntology.setForeground(Color.DARK_GRAY);
      pnlTargetOntology.add(lblTargetOntology);

      JLabel lblOntologyID = new JLabel(getTitle(ontology));
      lblOntologyID.setForeground(Color.DARK_GRAY);
      pnlTargetOntology.add(lblOntologyID);

      ViewSplitPane splitPane = new ViewSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitPane.setResizeWeight(0.4);
      add(splitPane, BorderLayout.CENTER);

      /*
       * Workbook sheet GUI presentation
       */
      workbookView = new WorkbookView(this);
      splitPane.setTopComponent(workbookView);

      /*
       * Transformation rule browser, create, edit, remove panel
       */
      ruleBrowserView = new TransformationRuleBrowserView(this);
      splitPane.setBottomComponent(ruleBrowserView);

      validate();
   }

   public Set<OWLAxiom> doTransformation() throws ParseException {
      Set<OWLAxiom> results = Sets.newHashSet();
      TransformationRuleSet transformationRules = getRuleBrowserView().getPickedRules();
      for (TransformationRule rule : transformationRules) {
         String ruleString = rule.getRuleExpression();
         results.addAll(renderer.render(ruleString, workbook,
               new RenderingContext(rule.getSheetName(),
                     rule.getStartColumnIndex(),
                     rule.getEndColumnIndex(),
                     rule.getStartRowIndex(),
                     rule.getEndRowIndex())));
      }
      return results;
   }

   public WorkbookView getWorkbookView() {
      return workbookView;
   }

   public TransformationRuleBrowserView getRuleBrowserView() {
      return ruleBrowserView;
   }

   public OWLOntology getOntology() {
      return ontology;
   }

   public Workbook getWorkbook() {
      return workbook;
   }

   public OWLEditorKit getEditorKit() {
      return editorKit;
   }

   public boolean shouldClose() {
      return ruleBrowserView.safeGuardChanges();
   }

   public static JDialog createDialog(@Nonnull JComponent parent, @Nonnull File workbookFile,
         @Nonnull OWLEditorKit editorKit) throws Exception {
      JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, parent);
      final JDialog dialog = new JDialog(parentFrame, "Cellfie", Dialog.ModalityType.MODELESS);

      final OWLOntology ontology = editorKit.getOWLModelManager().getActiveOntology();
      final Workbook workbook = new Workbook(WorkbookFactory.create(workbookFile));
      final OwlEntityResolver entityResolver = new ProtegeEntityResolver(editorKit.getModelManager());
      final OwlRenderer renderer = new OwlRenderer(new OwlFactory(entityResolver));
      final CellfieWorkspace workspacePanel = new CellfieWorkspace(ontology, workbook, renderer, editorKit);
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
