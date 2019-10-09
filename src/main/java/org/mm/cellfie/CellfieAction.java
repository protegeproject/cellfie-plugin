package org.mm.cellfie;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JDialog;
import org.mm.cellfie.ui.CellfieWorkspace;
import org.mm.cellfie.ui.DialogUtils;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class CellfieAction extends ProtegeOWLAction {

   private static final long serialVersionUID = 1L;

   private static final Logger logger = LoggerFactory.getLogger(CellfieAction.class);

   private OWLWorkspace protegeWorkspace;

   @Override
   public void initialise() throws Exception {
      protegeWorkspace = getOWLWorkspace();
   }

   @Override
   public void actionPerformed(ActionEvent event) {
      File workbookFile = DialogUtils.showOpenFileChooser(protegeWorkspace,
            "Open Excel Workbook",
            "Excel Workbook (.xlsx, .xls)",
            "xlsx", "xls");
      if (workbookFile != null) {
         try {
            showCellfieDialog(workbookFile);
         } catch (Exception e) {
            String message = "Error starting Cellfie (see log for details)";
            ErrorLogPanel.showErrorDialog(e);
            logger.error(message, e);
         }
      }
   }

   private void showCellfieDialog(File workbookFile) throws Exception {
      JDialog cellfieDialog = CellfieWorkspace.createDialog(protegeWorkspace, workbookFile, getOWLEditorKit());
      cellfieDialog.setVisible(true);
   }

   @Override
   public void dispose() throws Exception {
      // NO-OP
   }
}
