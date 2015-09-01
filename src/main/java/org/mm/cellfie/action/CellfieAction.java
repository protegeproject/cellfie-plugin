package org.mm.cellfie.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.mm.ui.dialog.MMDialogManager;
import org.mm.ui.view.ApplicationView;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.OWLOntology;

public class CellfieAction extends ProtegeOWLAction
{
	private static final long serialVersionUID = 1L;

	private OWLOntology currentOntology;

	private JDialog cellfieDialog;

	@Override
	public void initialise() throws Exception
	{
		currentOntology = getOWLModelManager().getActiveOntology();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		ApplicationView appView = new ApplicationView(currentOntology, new ProtegeDialogManager());
		appView.setDividerLocation(500);
		appView.setResizeWeight(0.8);
		
		cellfieDialog = new JDialog();
		cellfieDialog.setTitle("Cellfie Spreadsheet Importer");
		cellfieDialog.setContentPane(appView);
		cellfieDialog.setSize(1100, 1000);
		cellfieDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		cellfieDialog.setVisible(true);
	}

	@Override
	public void dispose() throws Exception
	{
		cellfieDialog.dispose();
	}

	class ProtegeDialogManager implements MMDialogManager
	{
		@Override
		public int showConfirmDialog(Component parent, String title, String message)
		{
			return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
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
		public File showOpenFileChooser(Component parent, String message, String fileExtension, String fileDescription)
		{
			Set<String> extensions = new HashSet<>();
			extensions.add(fileExtension);
			return UIUtil.openFile(new JDialog(), "Open", fileDescription, extensions);
		}

		@Override
		public File showSaveFileChooser(Component parent, String message, String fileExtension, String fileDescription, boolean overwrite)
		{
			Set<String> extensions = new HashSet<>();
			extensions.add(fileExtension);
			return UIUtil.saveFile(new JDialog(), "Save", fileDescription, extensions, null);
		}
	}
}
