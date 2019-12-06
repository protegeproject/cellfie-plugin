package org.mm.cellfie;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JDialog;
import org.mm.cellfie.transformationrule.TransformationExecutor;
import org.mm.cellfie.ui.CellfieWorkspace;
import org.mm.cellfie.ui.DialogUtils;
import org.mm.renderer.owl.OwlRenderer;
import org.protege.editor.core.ui.error.ErrorLogPanel;
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

   private OwlRenderer renderer;

   @Override
   public void initialise() throws Exception {
      renderer = new OwlRenderer(new ProtegeEntityResolver(getOWLModelManager()));
   }

   @Override
   public void actionPerformed(ActionEvent event) {
      File workbookFile = DialogUtils.showOpenFileChooser(getOWLWorkspace(),
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
      
      JDialog cellfieDialog = CellfieWorkspace.createDialog(
            getOWLWorkspace(),
            workbookFile,
            getOWLEditorKit(),
            new TransformationExecutor(renderer));
      cellfieDialog.setLocationRelativeTo(null);
      cellfieDialog.setVisible(true);
   }

   @Override
   public void dispose() throws Exception {
      // NO-OP
   }
}
