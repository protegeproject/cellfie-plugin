package org.mm.cellfie.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.mm.cellfie.ui.view.WorkspacePanel;
import org.mm.ui.DialogManager;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.OWLOntology;

public class CellfieAction extends ProtegeOWLAction
{
   private static final long serialVersionUID = 1L;

   private DialogManager dialogManager;
   private OWLEditorKit editorKit;

   @Override
   public void initialise() throws Exception
   {
      dialogManager = new ProtegeDialogManager();
      editorKit = getOWLEditorKit();
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      File file = dialogManager.showOpenFileChooser(null, "Open Excel Workbook", "xlsx, xls", "Excel Workbook (.xlsx, .xls)");
      if (file != null) {
         String filename = file.getAbsolutePath();
         try {
            showCellfieDialog(filename);
         } catch (Exception ex) {
            dialogManager.showErrorMessageDialog(null, "Error opening file " + filename);
            ex.printStackTrace();
         }
      }
   }

   private void showCellfieDialog(String workbookPath)
   {
      final OWLOntology currentOntology = getOWLModelManager().getActiveOntology();
      final OWLWorkspace editorWindow = editorKit.getOWLWorkspace();
      JDialog cellfieDialog = WorkspacePanel.createDialog(currentOntology, workbookPath, editorKit, dialogManager);
      cellfieDialog.setLocationRelativeTo(editorWindow);
      cellfieDialog.setVisible(true);
   }

   @Override
   public void dispose() throws Exception
   {
      // NO-OP
   }

   class ProtegeDialogManager implements DialogManager
   {
      @Override
      public int showConfirmDialog(Component parent, String title, String message)
      {
         return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
      }

      @Override
      public String showInputDialog(Component parent, String message)
      {
         return JOptionPane.showInputDialog(parent, message, "Input", JOptionPane.OK_CANCEL_OPTION);
      }

      @Override
      public void showMessageDialog(Component parent, String message)
      {
         JOptionPane.showMessageDialog(parent, message, "Message", JOptionPane.INFORMATION_MESSAGE);
      }

      @Override
      public void showErrorMessageDialog(Component parent, String message)
      {
         JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
      }

      @Override
      public File showOpenFileChooser(Component parent, String title, String fileExtension, String fileDescription)
      {
         Set<String> extensions = new HashSet<>();
         for (String ext : fileExtension.split(",")) {
            extensions.add(ext.trim());
         }
         return UIUtil.openFile(parent, title, fileDescription, extensions);
      }

      @Override
      public File showSaveFileChooser(Component parent, String title, String fileExtension, String fileDescription,
            boolean overwrite)
      {
         Set<String> extensions = new HashSet<>();
         extensions.add(fileExtension);
         return UIUtil.saveFile(parent, title, fileDescription, extensions, null);
      }
   }
}
