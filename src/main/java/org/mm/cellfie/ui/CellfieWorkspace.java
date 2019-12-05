package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.mm.cellfie.transformationrule.TransformationRule.ANY_WILDCARD;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mm.cellfie.ProtegeEntityResolver;
import org.mm.cellfie.transformationrule.TransformationRule;
import org.mm.cellfie.transformationrule.TransformationRuleList;
import org.mm.parser.ParseException;
import org.mm.renderer.RenderingContext;
import org.mm.renderer.Sheet;
import org.mm.renderer.Workbook;
import org.mm.renderer.internal.CellUtils;
import org.mm.renderer.owl.OwlEntityResolver;
import org.mm.renderer.owl.OwlRenderer;
import org.protege.editor.core.ui.split.ViewSplitPane;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
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

      ViewSplitPane splitPane = new ViewSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      splitPane.setResizeWeight(1);
      add(splitPane, BorderLayout.CENTER);

      /*
       * Workbook sheet GUI presentation
       */
      workbookView = new WorkbookView(this);
      splitPane.setLeftComponent(workbookView);

      /*
       * Transformation rule browser, create, edit, remove panel
       */
      ruleBrowserView = new TransformationRuleBrowserView(this);
      splitPane.setRightComponent(ruleBrowserView);

      JPanel pnlGenerateAxioms = new JPanel(new BorderLayout());
      add(pnlGenerateAxioms, BorderLayout.SOUTH);

      JButton cmdGenerateAxioms = new JButton("Generate OWL Axioms");
      cmdGenerateAxioms.addActionListener(new GenerateAxiomsAction(this));
      pnlGenerateAxioms.add(cmdGenerateAxioms, BorderLayout.CENTER);

      validate();
   }

   public void setWorkbookTitle(String title) {
      workbookView.setTitle(title);
   }

   public Set<OWLAxiom> doTransformation() throws ParseException {
      Set<OWLAxiom> results = Sets.newHashSet();
      TransformationRuleList transformationRules = getRuleBrowserView().getPickedRules();
      final OwlEntityResolver entityResolver = new ProtegeEntityResolver(editorKit.getModelManager());
      for (TransformationRule rule : transformationRules) {
         String ruleString = rule.getRuleExpression();
         results.addAll(renderer.render(ruleString, workbook,
               new RenderingContext(rule.getSheetName(),
                     getStartColumnNumber(rule.getStartColumn()),
                     getEndColumnNumber(rule.getEndColumn(), workbook.getSheet(rule.getSheetName())),
                     getStartRowNumber(rule.getStartRow()),
                     getEndRowNumber(rule.getEndRow(), workbook.getSheet(rule.getSheetName()))),
               entityResolver));
      }
      return results;
   }

   private int getStartColumnNumber(String startColumn) {
      return CellUtils.toColumnNumber(startColumn);
   }

   private int getEndColumnNumber(String endColumn, Sheet sheet) {
      return (ANY_WILDCARD.equals(endColumn)) ? sheet.getEndColumnNumber() : CellUtils.toColumnNumber(endColumn);
   }

   private int getStartRowNumber(String startRow) {
      return CellUtils.toRowNumber(startRow);
   }

   private int getEndRowNumber(String endRow, Sheet sheet) {
      return (ANY_WILDCARD.equals(endRow)) ? sheet.getEndRowNumber() : CellUtils.toRowNumber(endRow);
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
      final OwlRenderer renderer = new OwlRenderer();
      final CellfieWorkspace workspacePanel = new CellfieWorkspace(ontology, workbook, renderer, editorKit);
      workspacePanel.setWorkbookTitle(workbookFile.getAbsolutePath().toString());
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
      dialog.setSize(1500, 900);
      dialog.setResizable(true);
      return dialog;
   }
}
