package org.mm.cellfie.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.mm.cellfie.ui.view.ApplicationView;
import org.mm.cellfie.ui.view.MappingBrowserView;
import org.mm.exceptions.MappingMasterException;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ss.SpreadSheetUtil;
import org.mm.ui.DialogManager;

public class CreateMappingExpressionDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private ApplicationView container;

	private int selectedRow = -1;

	private JComboBox<String> cbbSheetName;

	private JTextField txtStartColumn;
	private JTextField txtEndColumn;
	private JTextField txtStartRow;
	private JTextField txtEndRow;
	private JTextField txtComment;

	private JTextArea txtExpression;

	public CreateMappingExpressionDialog(ApplicationView container, SpreadSheetDataSource spreadSheet)
	{
		this.container = container;

		setTitle("MappingMaster Expression Dialog");
		setLocationRelativeTo(container);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		contentPane.add(pnlMain, BorderLayout.CENTER);

		JLabel lblSheetName = new JLabel("Sheet name:");
		cbbSheetName = new JComboBox<>();
		cbbSheetName.setModel(new DefaultComboBoxModel<>(new Vector<>(spreadSheet.getSheetNames())));

		JLabel lblStartColumn = new JLabel("Start column:");
		txtStartColumn = new JTextField("");

		JLabel lblEndColumn = new JLabel("End column:");
		txtEndColumn = new JTextField("");

		JLabel lblStartRow = new JLabel("Start row:");
		txtStartRow = new JTextField("");

		JLabel lblEndRow = new JLabel("End row:");
		txtEndRow = new JTextField("");

		JLabel lblComment = new JLabel("Comment:");
		txtComment = new JTextField("");

		JLabel lblExpression = new JLabel("DSL mapping expression:");

		JPanel pnlFields = new JPanel(new GridLayout(7, 2));
		pnlFields.add(lblSheetName);
		pnlFields.add(cbbSheetName);
		pnlFields.add(lblStartColumn);
		pnlFields.add(txtStartColumn);
		pnlFields.add(lblEndColumn);
		pnlFields.add(txtEndColumn);
		pnlFields.add(lblStartRow);
		pnlFields.add(txtStartRow);
		pnlFields.add(lblEndRow);
		pnlFields.add(txtEndRow);
		pnlFields.add(lblComment);
		pnlFields.add(txtComment);
		pnlFields.add(lblExpression);

		pnlMain.add(pnlFields, BorderLayout.NORTH);

		txtExpression = new JTextArea("", 20, 48);
		pnlMain.add(txtExpression, BorderLayout.CENTER);

		JPanel pnlCommands = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton cmdCancel = new JButton("Cancel");
		cmdCancel.setPreferredSize(new Dimension(100, 30));
		cmdCancel.addActionListener(new CancelActionListener());

		JButton cmdOK = new JButton("Save Changes");
		cmdOK.setPreferredSize(new Dimension(150, 30));
		cmdOK.addActionListener(new SaveChangesActionListener());

		pnlCommands.add(cmdCancel);
		pnlCommands.add(cmdOK);

		pnlMain.add(pnlCommands, BorderLayout.SOUTH);

		pack();
	}

	public void fillDialogFields(int rowIndex, String sheetName, String startColumn, String endColumn, String startRow, String endRow, String expression, String comment)
	{
		selectedRow = rowIndex;

		cbbSheetName.setSelectedItem(sheetName);

		txtStartColumn.setText(startColumn);
		txtEndColumn.setText(endColumn);
		txtStartRow.setText(startRow);
		txtEndRow.setText(endRow);

		txtComment.setText(comment);
		txtExpression.setText(expression);
	}

	private class CancelActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}
	}

	private class SaveChangesActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			try {
				String sheetName = (String) cbbSheetName.getSelectedItem();

				String startColumn = txtStartColumn.getText().trim().toUpperCase();
				String endColumn = txtEndColumn.getText().trim().toUpperCase();
				SpreadSheetUtil.checkColumnSpecification(startColumn);
				SpreadSheetUtil.checkColumnSpecification(endColumn);
				
				String startRow = txtStartRow.getText().trim();
				String endRow = txtEndRow.getText().trim();
				
				String comment = txtComment.getText().trim();
				String expression = txtExpression.getText().trim();
				
				getMappingBrowserView().updateTableModel(selectedRow, sheetName, startColumn, endColumn, startRow, endRow, expression, comment);
				
				setVisible(false);
			}
			catch (MappingMasterException ex) {
				getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
			}
		}
	}

	private MappingBrowserView getMappingBrowserView()
	{
		return container.getMappingBrowserView();
	}

	private DialogManager getApplicationDialogManager()
	{
		return container.getApplicationDialogManager();
	}
}
