package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mm.cellfie.transformationrule.TransformationExecutor;
import org.mm.cellfie.transformationrule.TransformationRuleList;
import org.mm.renderer.Workbook;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.core.ui.split.ViewSplitPane;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

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
   private final TransformationExecutor executor;
   private final OWLEditorKit editorKit;

   private final WorkbookView workbookView;
   private final TransformationRuleBrowserView ruleBrowserView;

   public CellfieWorkspace(@Nonnull OWLOntology ontology, @Nonnull Workbook workbook,
         @Nonnull TransformationExecutor executor, @Nonnull OWLEditorKit editorKit) {
      this.ontology = checkNotNull(ontology);
      this.workbook = checkNotNull(workbook);
      this.executor = checkNotNull(executor);
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

      validate();
   }

   public void setWorkbookTitle(String title) {
      workbookView.setTitle(title);
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

   public TransformationRuleList getSelectedRules() {
      return ruleBrowserView.getSelectedRules();
   }

   public TransformationExecutor getTransformationExecutor() {
      return executor;
   }

   public OWLEditorKit getEditorKit() {
      return editorKit;
   }

   public Collection<OWLAxiom> generateAxioms() {
      return executor.execute(getWorkbook(), getSelectedRules());
   }

   public boolean shouldClose() {
      return ruleBrowserView.safeGuardChanges();
   }

   public static JDialog createDialog(@Nonnull JComponent parent, @Nonnull File workbookFile,
         @Nonnull OWLEditorKit editorKit, @Nonnull TransformationExecutor executor) throws Exception {
      JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, parent);
      final JDialog dialog = new JDialog(parentFrame, "Cellfie", Dialog.ModalityType.MODELESS);

      final OWLModelManager modelManager = editorKit.getOWLModelManager();
      final OWLOntology ontology = modelManager.getActiveOntology();
      final Workbook workbook = new Workbook(WorkbookFactory.create(workbookFile));
      final CellfieWorkspace cellfieWorkspace = new CellfieWorkspace(ontology, workbook, executor, editorKit);
      cellfieWorkspace.setWorkbookTitle(workbookFile.getAbsolutePath().toString());
      cellfieWorkspace.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

      // Closing Cellfie using ESC key
      cellfieWorkspace.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CLOSE_DIALOG");
      cellfieWorkspace.getActionMap().put("CLOSE_DIALOG", new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent event) {
            int answer = DialogUtils.showConfirmDialog(dialog, "Exit Cellfie?");
            switch (answer) {
               case JOptionPane.YES_OPTION :
                  if (cellfieWorkspace.shouldClose()) {
                     dialog.setVisible(false);
                  }
            }
         }
      });

      cellfieWorkspace.getInputMap(JTable.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "GENERATE_AXIOMS");
      cellfieWorkspace.getActionMap().put("GENERATE_AXIOMS", new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent event) {
            try {
               ResultDialog.showDialog(cellfieWorkspace, cellfieWorkspace.generateAxioms());
            } catch (Exception e) {
               ErrorLogPanel.showErrorDialog(e);
            }
         }
      });

      // Closing Cellfie using close [x] button 
      dialog.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            if (cellfieWorkspace.shouldClose()) {
               dialog.setVisible(false);
            }
         }
      });

      dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      dialog.setContentPane(cellfieWorkspace);
      dialog.setSize(1500, 900);
      dialog.setResizable(true);
      return dialog;
   }
}
