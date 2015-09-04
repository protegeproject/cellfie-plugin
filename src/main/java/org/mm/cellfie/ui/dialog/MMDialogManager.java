package org.mm.cellfie.ui.dialog;

import java.awt.Component;
import java.io.File;

/*
 * See https://github.com/protegeproject/swrlapi/blob/master/src/main/java/org/swrlapi/factory/DefaultSWRLAPIDialogManager.java
 * for a potential implementation.
 */
public interface MMDialogManager
{
	int showConfirmDialog(Component parent, String title, String message);

	void showMessageDialog(Component parent, String message);

	void showErrorMessageDialog(Component parent, String message);

	File showOpenFileChooser(Component parent, String message, String fileExtension, String fileDescription);

	File showSaveFileChooser(Component parent, String message, String fileExtension, String fileDescription, boolean overwrite);
}
