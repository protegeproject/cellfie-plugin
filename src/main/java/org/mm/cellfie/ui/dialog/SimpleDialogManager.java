package org.mm.cellfie.ui.dialog;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SimpleDialogManager implements MMDialogManager
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
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Open");
		
		if (fileExtension == null || fileExtension.isEmpty()) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		else {
			chooser.setFileFilter(new FileNameExtensionFilter(fileDescription, fileExtension));
		}
		
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	@Override
	public File showSaveFileChooser(Component parent, String message, String fileExtension, String fileDescription, boolean overwrite)
	{
		JFileChooser chooser = new JFileChooser()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void approveSelection()
			{
				if (!overwrite) { return; }
				
				File f = getSelectedFile();
				if (f.exists()) {
					String msg = "The file '" + f.getName() + "' already exists!\nDo you want to replace it?";
					int option = JOptionPane.showConfirmDialog(this, msg, "Overwrite file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (option == JOptionPane.NO_OPTION) {
						return;
					}
				}
				super.approveSelection();
			}
		};
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle("Save");
		
		if (fileExtension != null) {
			chooser.setFileFilter(new FileNameExtensionFilter(fileExtension, fileDescription));
		}
		
		if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
}
