package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mm.cellfie.ui.exception.CellfieException;
import org.mm.core.MappingExpression;
import org.mm.core.MappingExpressionSetFactory;
import org.mm.ui.DialogManager;
import org.mm.ui.ModelView;
import org.protege.editor.core.ui.util.JOptionPaneEx;

public class MappingBrowserView extends JPanel implements ModelView
{
	private static final long serialVersionUID = 1L;

	private ApplicationView container;

	private JButton cmdRunMapping;
	private JButton cmdAdd;
	private JButton cmdEdit;
	private JButton cmdDelete;
	private JButton cmdSave;

	private JTextField txtMappingPath;
	private JTable tblMappingExpression;

	private MappingExpressionTableModel tableModel;

	private int selectedRow = -1; // -1 means no row selection

	private static final int CANCEL_OPTION = 0;
	private static final int SAVE_CHANGES_OPTION = 1;

	public MappingBrowserView(ApplicationView container)
	{
		this.container = container;
		
		tblMappingExpression = new JTable();
		tblMappingExpression.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblMappingExpression.setGridColor(Color.LIGHT_GRAY);
		tblMappingExpression.setDefaultRenderer(String.class, new MultiLineCellRenderer());

		JScrollPane scrMappingExpression = new JScrollPane(tblMappingExpression);
		
		setLayout(new BorderLayout());
		
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mapping File"));

		add(pnlTop, BorderLayout.NORTH);

		txtMappingPath = new JTextField();
		txtMappingPath.setPreferredSize(new Dimension(80, 30));
		txtMappingPath.setEnabled(true);
		pnlTop.add(txtMappingPath, BorderLayout.CENTER);

		JPanel pnlMappingOpenSave = new JPanel(new GridLayout(1, 4));
		pnlTop.add(pnlMappingOpenSave, BorderLayout.EAST);

		JButton cmdOpen = new JButton("Browse...");
		cmdOpen.addActionListener(new OpenMappingAction());
		pnlMappingOpenSave.add(cmdOpen);

		cmdSave = new JButton("Save");
		cmdSave.addActionListener(new SaveMappingAction());
		cmdSave.setEnabled(false);
		pnlMappingOpenSave.add(cmdSave);

		JButton cmdSaveAs = new JButton("Save As...");
		cmdSaveAs.addActionListener(new SaveAsMappingAction());
		pnlMappingOpenSave.add(cmdSaveAs);
		
		JPanel pnlCenter = new JPanel(new BorderLayout());
		add(pnlCenter, BorderLayout.CENTER);
		
		pnlCenter.add(scrMappingExpression, BorderLayout.CENTER);
		
		JPanel pnlBottom = new JPanel(new BorderLayout());
		add(pnlBottom, BorderLayout.SOUTH);
		
		JPanel pnlCommandButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlBottom.add(pnlCommandButton, BorderLayout.WEST);
		
		cmdAdd = new JButton("Add");
		cmdAdd.addActionListener(new AddButtonActionListener());
		pnlCommandButton.add(cmdAdd);
		
		cmdEdit = new JButton("Edit");
		cmdEdit.addActionListener(new EditButtonActionListener());
		pnlCommandButton.add(cmdEdit);
		
		cmdDelete = new JButton("Delete");
		cmdDelete.addActionListener(new DeleteButtonActionListener());
		pnlCommandButton.add(cmdDelete);
		
		JPanel pnlRunMappingButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlBottom.add(pnlRunMappingButton, BorderLayout.EAST);
		
		cmdRunMapping = new JButton("Run Mapping");
		cmdRunMapping.addActionListener(new MapExpressionsAction(container));
		pnlRunMappingButton.add(cmdRunMapping);
		
		validate();
	}

	public String getMappingFilename()
	{
		File file = new File(txtMappingPath.getText());
		return file.getName();
	}

	public String getResourcePath()
	{
		File file = new File(txtMappingPath.getText());
		return file.getPath();
	}

	@Override
	public void update()
	{
		try {
			tableModel = new MappingExpressionTableModel(container.getLoadedMappingExpressions());
			tblMappingExpression.setModel(tableModel);
			setPreferredColumnSize();
			resizeColumnHeight();
		} catch (CellfieException ex) {
			container.getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
		}
	}

	public void updateTableModel(String sheetName, String startColumn, String endColumn, String startRow, String endRow, String expression, String comment)
	{
		Vector<String> row = new Vector<>();
		row.add(0, sheetName);
		row.add(1, startColumn);
		row.add(2, endColumn);
		row.add(3, startRow);
		row.add(4, endRow);
		row.add(5, expression);
		row.add(6, comment);
		
		if (selectedRow != -1) { // user selected a row
			tableModel.removeRow(selectedRow);
		}
		tableModel.addRow(row);
		resizeColumnHeight();
	}

	private void setPreferredColumnSize()
	{
		final TableColumnModel columnModel = tblMappingExpression.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(80);
		columnModel.getColumn(1).setPreferredWidth(80);
		columnModel.getColumn(2).setPreferredWidth(80);
		columnModel.getColumn(3).setPreferredWidth(80);
		columnModel.getColumn(4).setPreferredWidth(80);
		columnModel.getColumn(5).setPreferredWidth(400);
		columnModel.getColumn(6).setPreferredWidth(280);
	}

	private void resizeColumnHeight()
	{
		for (int column = 0; column < tblMappingExpression.getColumnCount(); column++) {
			if (column == 5) {
				for (int row = 0; row < tblMappingExpression.getRowCount(); row++) {
					int height = 35; // min height;
					Object value = tblMappingExpression.getModel().getValueAt(row, column);
					TableCellRenderer renderer = tblMappingExpression.getDefaultRenderer(String.class);
					Component comp = renderer.getTableCellRendererComponent(tblMappingExpression, value, false, false, row, column);
					height = Math.max(comp.getPreferredSize().height, height);
					tblMappingExpression.setRowHeight(row, height);
				}
			}
		}
	}

	public List<MappingExpression> getMappingExpressions()
	{
		return tableModel.getMappingExpressions();
	}

	private DialogManager getApplicationDialogManager()
	{
		return container.getApplicationDialogManager();
	}

	class MappingExpressionTableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = 1L;

		private final String[] COLUMN_NAMES = {
			"Sheet Name", "Start Column", "End Column", "Start Row", "End Row", "Mapping Expression", "Comment"
		};

		public MappingExpressionTableModel(final List<MappingExpression> mappings)
		{
			super();
			for (MappingExpression mapping : mappings) {
				Vector<Object> row = new Vector<Object>();
				row.add(mapping.getSheetName());
				row.add(mapping.getStartColumn());
				row.add(mapping.getEndColumn());
				row.add(mapping.getStartRow());
				row.add(mapping.getEndRow());
				row.add(mapping.getExpressionString());
				row.add(mapping.getComment());
				addRow(row);
			}
		}

		@Override
		public String getColumnName(int column) // 0-based
		{
			return COLUMN_NAMES[column];
		}

		@Override
		public int getColumnCount()
		{
			return COLUMN_NAMES.length;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}

		public List<MappingExpression> getMappingExpressions()
		{
			List<MappingExpression> mappings = new ArrayList<>();
			for (int row = 0; row < getRowCount(); row++) {
				@SuppressWarnings("unchecked")
				Vector<String> rowVector = (Vector<String>) getDataVector().elementAt(row);
				String sheetName = rowVector.get(0);
				String startColumn = rowVector.get(1);
				String endColumn = rowVector.get(2);
				String startRow = rowVector.get(3);
				String endRow = rowVector.get(4);
				String expression = rowVector.get(5);
				String comment = rowVector.get(6);
				mappings.add(new MappingExpression(sheetName, startColumn, endColumn, startRow, endRow, comment, expression));
			}
			return mappings;
		}
	}

	/**
	 * To allow cells in the mapping browser table to have multilines.
	 */
	class MultiLineCellRenderer extends JTextArea implements TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setFont(table.getFont());
			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					setForeground(UIManager.getColor("Table.focusCellForeground"));
					setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				setBorder(new EmptyBorder(1, 2, 1, 2));
			}
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	/*
	 * Action listener implementations for command buttons in MappingExpressionView panel
	 */

	class AddButtonActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try {
				selectedRow = -1;
				MappingExpressionEditorPanel editorPanel = new MappingExpressionEditorPanel();
				editorPanel.setSheetNames(container.getLoadedSpreadSheet().getSheetNames());
				showMappingEditorDialog(editorPanel);
			} catch (CellfieException ex) {
				container.getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
			}
		}
	}

	class EditButtonActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			selectedRow = tblMappingExpression.getSelectedRow();
			if (selectedRow == -1) {
				getApplicationDialogManager().showMessageDialog(container, "No mapping expression was selected");
				return;
			}
			try {
				MappingExpressionEditorPanel editorPanel = new MappingExpressionEditorPanel();
				editorPanel.setSheetNames(container.getLoadedSpreadSheet().getSheetNames());
				editorPanel.fillFormFields(
						getValueAt(selectedRow, 0),
						getValueAt(selectedRow, 1),
						getValueAt(selectedRow, 2),
						getValueAt(selectedRow, 3),
						getValueAt(selectedRow, 4),
						getValueAt(selectedRow, 5),
						getValueAt(selectedRow, 6));
				showMappingEditorDialog(editorPanel);
			} catch (CellfieException ex) {
				container.getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
			}
		}

		private String getValueAt(int row, int column)
		{
			return (String) tableModel.getValueAt(row, column);
		}
	}

	private void showMappingEditorDialog(MappingExpressionEditorPanel editorPanel)
	{
		final SaveOption[] options = { 
				new SaveOption(CANCEL_OPTION, "Cancel"),
				new SaveOption(SAVE_CHANGES_OPTION, "Save Changes")
		};
		try {
			int answer = JOptionPaneEx.showConfirmDialog(container, "Mapping Editor", editorPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, null);
			switch (answer) {
				case SAVE_CHANGES_OPTION:
					MappingExpression userInput = editorPanel.getUserInput();
					updateTableModel(
							userInput.getSheetName(),
							userInput.getStartColumn(),
							userInput.getEndColumn(),
							userInput.getStartRow(),
							userInput.getEndRow(),
							userInput.getExpressionString(),
							userInput.getComment());
					break;
			}
		} catch (ClassCastException ex) {
			// NO-OP: Fix should be to Protege API
		}
	}

	class DeleteButtonActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			selectedRow = tblMappingExpression.getSelectedRow();
			if (selectedRow == -1) {
				getApplicationDialogManager().showMessageDialog(container, "No mapping expression was selected");
				return;
			}
			int answer = getApplicationDialogManager().showConfirmDialog(container, "Delete", "Do you really want to delete the selected expression?");
			if (answer == JOptionPane.YES_OPTION) {
				tableModel.removeRow(selectedRow);
			}
		}
	}

	class OpenMappingAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try {
				File file = getApplicationDialogManager().showOpenFileChooser(
						container, "Open", "json", "MappingMaster DSL Mapping Expression (.json)");
				if (file != null) {
					String filePath = file.getAbsolutePath();
					container.loadMappingDocument(filePath);
					txtMappingPath.setText(filePath);
					cmdSave.setEnabled(true);
				}
			} catch (Exception ex) {
				getApplicationDialogManager().showErrorMessageDialog(container, "Error opening file: " + ex.getMessage());
				txtMappingPath.setText("");
			}
		}
	}

	class SaveMappingAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try {
				MappingExpressionSetFactory.saveMappingExpressionSetToDocument(txtMappingPath.getText(), tableModel.getMappingExpressions());
				container.updateMappingExpressionModel(tableModel.getMappingExpressions());
			}
			catch (IOException ex) {
				getApplicationDialogManager().showErrorMessageDialog(container, "Error saving file: " + ex.getMessage());
			}
		}
	}

	class SaveAsMappingAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try {
				File file = getApplicationDialogManager().showSaveFileChooser(
						container, "Save As", "json", "MappingMaster DSL Mapping Expression (.json)", true);
				if (file != null) {
					String filePath = file.getAbsolutePath();
					String ext = ".json";
					if (!filePath.endsWith(ext)) {
						filePath = filePath + ext;
					}
					MappingExpressionSetFactory.saveMappingExpressionSetToDocument(filePath, tableModel.getMappingExpressions());
					container.updateMappingExpressionModel(tableModel.getMappingExpressions());
					txtMappingPath.setText(filePath);
				}
			} catch (Exception ex) {
				getApplicationDialogManager().showErrorMessageDialog(container, "Error saving file: " + ex.getMessage());
				txtMappingPath.setText("");
			}
		}
	}

	/**
	 * A helper class for creating mapping editor command buttons.
	 */
	class SaveOption implements Comparable<SaveOption>
	{
		private int option;
		private String title;
		
		public SaveOption(int option, String title)
		{
			this.option = option;
			this.title = title;
		}
		
		public int get()
		{
			return option;
		}
		
		@Override
		public String toString()
		{
			return title;
		}

		@Override
		public int compareTo(SaveOption o)
		{
			return option-o.option;
		}
	}
}
