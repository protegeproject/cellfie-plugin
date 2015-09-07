package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.poi.ss.usermodel.Sheet;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ui.DialogManager;
import org.mm.ui.ModelView;
import org.protege.editor.core.ui.tabbedpane.ViewTabbedPane;

public class DataSourceView extends JPanel implements ModelView
{
	private static final long serialVersionUID = 1L;

	private ApplicationView container;

	private JTextField txtWorkbookPath;
	private ViewTabbedPane tabSheetContainer;

	public DataSourceView(ApplicationView container)
	{
		this.container = container;

		setLayout(new BorderLayout());

		tabSheetContainer = new ViewTabbedPane();
		tabSheetContainer.addTab("NONE", new JPanel());
		add(tabSheetContainer, BorderLayout.CENTER);

		JPanel pnlWorkbookFile = new JPanel(new BorderLayout());
		pnlWorkbookFile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Spreadsheet File"));
		add(pnlWorkbookFile, BorderLayout.NORTH);

		txtWorkbookPath = new JTextField("");
		pnlWorkbookFile.add(txtWorkbookPath, BorderLayout.CENTER);

		JButton cmdOpen = new JButton("Browse...");
		cmdOpen.addActionListener(new OpenWorkbookAction());
		pnlWorkbookFile.add(cmdOpen, BorderLayout.EAST);

		validate();
	}

	private class OpenWorkbookAction implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			try {
				File file = getApplicationDialogManager().showOpenFileChooser(
						container, "Open Excel Workbook", "xlsx", "Excel Workbook (.xlsx)");
				if (file != null) {
					String filename = file.getAbsolutePath();
					container.loadWorkbookDocument(filename);
					txtWorkbookPath.setText(filename);
				}
			} catch (Exception ex) {
				getApplicationDialogManager().showErrorMessageDialog(container,
						"Error opening file: " + ex.getMessage());
			}
		}
	}

	private DialogManager getApplicationDialogManager()
	{
		return container.getApplicationDialogManager();
	}

	@Override
	public void update()
	{
		tabSheetContainer.removeAll(); // reset the tab panel first
		SpreadSheetDataSource spreadsheet = container.getLoadedSpreadSheet();
		for (Sheet sheet : spreadsheet.getSheets()) {
			SheetView sheetView = new SheetView(sheet);
			tabSheetContainer.addTab(sheet.getSheetName(), null, sheetView);
		}
	}
}
