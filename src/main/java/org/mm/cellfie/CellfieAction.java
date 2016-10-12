package org.mm.cellfie;

import static java.lang.String.format;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JDialog;

import org.mm.cellfie.ui.DialogUtils;
import org.mm.cellfie.ui.WorkspacePanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class CellfieAction extends ProtegeOWLAction {

   private static final long serialVersionUID = 1L;

   private static final Logger logger = LoggerFactory.getLogger(CellfieAction.class);

   private OWLEditorKit editorKit;

   @Override
   public void initialise() throws Exception {
      editorKit = getOWLEditorKit();
   }

   @Override
   public void actionPerformed(ActionEvent event) {
      final OWLWorkspace protegeWorkspace = editorKit.getOWLWorkspace();
      File workbookFile = DialogUtils.showOpenFileChooser(protegeWorkspace,
            "Open Excel Workbook",
            "Excel Workbook (.xlsx, .xls)",
            "xlsx", "xls");
      if (workbookFile != null) {
         String workbookFilePath = workbookFile.getAbsolutePath();
         try {
            showCellfieDialog(workbookFilePath);
         } catch (Exception e) {
            logger.error(e.getMessage(), e);
            DialogUtils.showErrorDialog(protegeWorkspace, format("Error opening file %s", workbookFilePath));
         }
      }
   }

   private void showCellfieDialog(String workbookPath) {
      final OWLOntology currentOntology = getOWLModelManager().getActiveOntology();
      final OWLWorkspace editorWindow = editorKit.getOWLWorkspace();
      JDialog cellfieDialog = WorkspacePanel.createDialog(currentOntology, workbookPath, editorKit);
      cellfieDialog.setLocationRelativeTo(editorWindow);
      cellfieDialog.setVisible(true);
   }

   @Override
   public void dispose() throws Exception {
      // NO-OP
   }
}
