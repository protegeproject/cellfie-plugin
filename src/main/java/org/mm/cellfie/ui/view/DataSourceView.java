package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.poi.ss.usermodel.Sheet;
import org.mm.cellfie.ui.exception.CellfieException;
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

		setLayout(new BorderLayout(4, 1));
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Spreadsheet"));

		tabSheetContainer = new ViewTabbedPane();
		tabSheetContainer.addTab("NONE", new JPanel());
		add(tabSheetContainer, BorderLayout.CENTER);

		JPanel pnlWorkbookFile = new JPanel(new BorderLayout());
		pnlWorkbookFile.setBorder(new EmptyBorder(2, 5, 7, 5));
		add(pnlWorkbookFile, BorderLayout.NORTH);

		JLabel lblDataSource = new JLabel("Data Source: ");
		pnlWorkbookFile.add(lblDataSource, BorderLayout.WEST);

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
				File file = getApplicationDialogManager().showOpenFileChooser(container, "Open Excel Workbook", "xlsx", "Excel Workbook (.xlsx)");
				if (file != null) {
					String filename = file.getAbsolutePath();
					container.loadWorkbookDocument(filename);
					txtWorkbookPath.setText(filename);
				}
			} catch (Exception ex) {
				getApplicationDialogManager().showErrorMessageDialog(container, "Error opening file: " + ex.getMessage());
				txtWorkbookPath.setText("");
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
		try {
			tabSheetContainer.removeAll(); // reset the tab panel first
			SpreadSheetDataSource spreadsheet = container.getLoadedSpreadSheet();
			for (Sheet sheet : spreadsheet.getSheets()) {
				SheetPanel sheetPanel = new SheetPanel(sheet);
				tabSheetContainer.addTab(sheetPanel.getSheetName(), null, sheetPanel);
			}
		} catch (CellfieException ex) {
			getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
		}
	}
}
