package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mm.cellfie.ui.exception.CellfieException;
import org.mm.core.TransformationRule;
import org.mm.core.TransformationRuleSetFactory;
import org.mm.ui.DialogManager;
import org.mm.ui.ModelView;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.JOptionPaneEx;

public class TransformationRuleBrowserView extends JPanel implements ModelView
{
   private static final long serialVersionUID = 1L;

   private WorkspacePanel container;

   private JPanel pnlContainer;

   private JButton cmdAdd;
   private JButton cmdEdit;
   private JButton cmdDelete;
   private JButton cmdSave;
   private JButton cmdSaveAs;
   private JButton cmdGenerateAxioms;

   private JTable tblTransformationRules;

   private TransformationRulesTableModel tableModel;

   public TransformationRuleBrowserView(WorkspacePanel container)
   {
      this.container = container;

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      pnlContainer = new JPanel();
      pnlContainer.setLayout(new BorderLayout());
      add(pnlContainer, BorderLayout.CENTER);

      tblTransformationRules = new JTable();
      tblTransformationRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      tblTransformationRules.setGridColor(new Color(220, 220, 220));
      tblTransformationRules.setDefaultRenderer(String.class, new MultiLineCellRenderer());
      tblTransformationRules.addMouseListener(new MappingExpressionSelectionListener());
      tblTransformationRules.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_RULE");
      tblTransformationRules.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_RULE");
      tblTransformationRules.getActionMap().put("DELETE_RULE", new DeleteRuleAction());

      JScrollPane scrMappingExpression = new JScrollPane(tblTransformationRules);

      JPanel pnlTop = new JPanel(new BorderLayout());
      pnlTop.setBorder(new EmptyBorder(2, 5, 7, 5));
      pnlContainer.add(pnlTop, BorderLayout.NORTH);

      JPanel pnlCommandButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pnlTop.add(pnlCommandButton, BorderLayout.WEST);

      cmdAdd = new JButton("Add");
      cmdAdd.setPreferredSize(new Dimension(72, 22));
      cmdAdd.addActionListener(new AddButtonActionListener());
      pnlCommandButton.add(cmdAdd);

      cmdEdit = new JButton("Edit");
      cmdEdit.setPreferredSize(new Dimension(72, 22));
      cmdEdit.setEnabled(false);
      cmdEdit.addActionListener(new EditButtonActionListener());
      pnlCommandButton.add(cmdEdit);

      cmdDelete = new JButton("Delete");
      cmdDelete.setPreferredSize(new Dimension(72, 22));
      cmdDelete.setEnabled(false);
      cmdDelete.addActionListener(new DeleteButtonActionListener());
      pnlCommandButton.add(cmdDelete);

      JPanel pnlMappingOpenSave = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pnlTop.add(pnlMappingOpenSave, BorderLayout.EAST);

      JButton cmdLoad = new JButton("Load Rules");
      cmdLoad.setPreferredSize(new Dimension(152, 22));
      cmdLoad.addActionListener(new OpenMappingAction());
      pnlMappingOpenSave.add(cmdLoad);

      cmdSave = new JButton("Save Rules");
      cmdSave.setPreferredSize(new Dimension(152, 22));
      cmdSave.addActionListener(new SaveMappingAction());
      cmdSave.setEnabled(false);
      pnlMappingOpenSave.add(cmdSave);

      cmdSaveAs = new JButton("Save As...");
      cmdSaveAs.setPreferredSize(new Dimension(152, 22));
      cmdSaveAs.addActionListener(new SaveAsMappingAction());
      cmdSaveAs.setEnabled(false);
      pnlMappingOpenSave.add(cmdSaveAs);

      JPanel pnlCenter = new JPanel(new BorderLayout());
      pnlContainer.add(pnlCenter, BorderLayout.CENTER);

      pnlCenter.add(scrMappingExpression, BorderLayout.CENTER);

      JPanel pnlGenerateAxioms = new JPanel();
      pnlContainer.add(pnlGenerateAxioms, BorderLayout.SOUTH);

      cmdGenerateAxioms = new JButton("Generate Axioms");
      cmdGenerateAxioms.setPreferredSize(new Dimension(152, 22));
      cmdGenerateAxioms.addActionListener(new GenerateAxiomsAction(container));
      cmdGenerateAxioms.setEnabled(false);
      pnlGenerateAxioms.add(cmdGenerateAxioms);

      update();
      validate();
   }

   @Override
   public void update()
   {
      tableModel = new TransformationRulesTableModel(container.getActiveTransformationRules());
      tblTransformationRules.setModel(tableModel);
      tblTransformationRules.getColumnModel().getColumn(0).setHeaderRenderer(
            new CheckBoxHeaderRenderer(tblTransformationRules.getTableHeader()));
      setTableHeaderAlignment(SwingConstants.CENTER);
      setPreferredColumnWidth();
      setPreferredColumnHeight();
      updateBorderUI();
   }

   private void setTableHeaderAlignment(int alignment)
   {
      ((DefaultTableCellRenderer) tblTransformationRules.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(alignment);
   }

   private void enableSaveButton()
   {
      cmdSave.setEnabled(true);
   }

   private void updateBorderUI()
   {
      pnlContainer.setBorder(ComponentFactory.createTitledBorder(getTitle()));
   }

   private String getTitle()
   {
      Optional<String> fileLocation = container.getRuleFileLocation();
      if (!fileLocation.isPresent()) {
         return String.format("Transformation Rules");
      }
      return String.format("Transformation Rules (%s)", fileLocation.get());
   }

   private void updateTableModel(int selectedRow, String sheetName, String startColumn, String endColumn,
         String startRow, String endRow, String expression, String comment)
   {
      Vector<Object> row = new Vector<>();
      row.add(0, true);
      row.add(1, sheetName);
      row.add(2, startColumn);
      row.add(3, endColumn);
      row.add(4, startRow);
      row.add(5, endRow);
      row.add(6, expression);
      row.add(7, comment);

      if (selectedRow != -1) { // user selected a row
         tableModel.removeRow(selectedRow);
      }
      tableModel.addRow(row);
   }

   private void setPreferredColumnWidth()
   {
      final TableColumnModel columnModel = tblTransformationRules.getColumnModel();
      columnModel.getColumn(0).setPreferredWidth(30);
      columnModel.getColumn(1).setPreferredWidth(100);
      columnModel.getColumn(2).setPreferredWidth(100);
      columnModel.getColumn(3).setPreferredWidth(100);
      columnModel.getColumn(4).setPreferredWidth(100);
      columnModel.getColumn(5).setPreferredWidth(100);
      columnModel.getColumn(6).setPreferredWidth(360);
      columnModel.getColumn(7).setPreferredWidth(180);
   }

   private void setPreferredColumnHeight()
   {
      int columnIndex = 6; // only for expression column
      for (int row = 0; row < tblTransformationRules.getRowCount(); row++) {
         int height = 0; // min height;
         Object value = tblTransformationRules.getModel().getValueAt(row, columnIndex);
         TableCellRenderer renderer = tblTransformationRules.getDefaultRenderer(String.class);
         Component comp = renderer.getTableCellRendererComponent(tblTransformationRules, value, false, false, row, columnIndex);
         height = Math.max(comp.getPreferredSize().height, height);
         tblTransformationRules.setRowHeight(row, height);
      }
   }

   public List<TransformationRule> getTransformationRules()
   {
      return tableModel.getTransformationRules();
   }

   private DialogManager getApplicationDialogManager()
   {
      return container.getApplicationDialogManager();
   }

   class TransformationRulesTableModel extends DefaultTableModel implements TableModelListener
   {
      private static final long serialVersionUID = 1L;

      private final String[] COLUMN_NAMES = { "", "Sheet Name", "Start Column", "End Column", "Start Row", "End Row",
            "Rule", "Comment" };

      private boolean hasUnsavedChanges = false;

      public TransformationRulesTableModel(final List<TransformationRule> rules)
      {
         super();
         for (TransformationRule rule : rules) {
            Vector<Object> row = new Vector<>();
            row.add(false);
            row.add(rule.getSheetName());
            row.add(rule.getStartColumn());
            row.add(rule.getEndColumn());
            row.add(rule.getStartRow());
            row.add(rule.getEndRow());
            row.add(rule.getRuleString());
            row.add(rule.getComment());
            addRow(row);
         }
         addTableModelListener(this);
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
         if (columnIndex == 0) {
            return Boolean.class;
         }
         return String.class;
      }

      @Override
      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
         if (columnIndex == 0) {
            return true;
         }
         return false;
      }

      public List<TransformationRule> getTransformationRules()
      {
         List<TransformationRule> rules = new ArrayList<>();
         for (int row = 0; row < getRowCount(); row++) {
            Vector<?> rowVector = (Vector<?>) getDataVector().elementAt(row);
            if (isRuleSelected(rowVector)) {
               String sheetName = String.valueOf(rowVector.get(1));
               String startColumn = String.valueOf(rowVector.get(2));
               String endColumn = String.valueOf(rowVector.get(3));
               String startRow = String.valueOf(rowVector.get(4));
               String endRow = String.valueOf(rowVector.get(5));
               String expression = String.valueOf(rowVector.get(6));
               String comment = String.valueOf(rowVector.get(7));
               rules.add(new TransformationRule(sheetName, startColumn, endColumn, startRow, endRow, comment, expression));
            }
         }
         return rules;
      }

      private boolean isRuleSelected(Vector<?> rowVector)
      {
         return ((Boolean) rowVector.get(0)) == true;
      }

      protected List<TransformationRule> getTransformationRulesAndSave()
      {
         hasUnsavedChanges = false;
         return getTransformationRules();
      }

      public boolean hasUnsavedChanges()
      {
         return hasUnsavedChanges;
      }

      @Override
      public void tableChanged(TableModelEvent e)
      {
         hasUnsavedChanges = true;
      }
   }

   /**
    * To allow cells in the mapping browser table to have multi-lines.
    */
   class MultiLineCellRenderer extends JTextArea implements TableCellRenderer
   {
      private static final long serialVersionUID = 1L;

      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
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

   /**
    * Header renderer for having a check box
    */
   class CheckBoxHeaderRenderer extends JCheckBox implements TableCellRenderer
   {
      private static final long serialVersionUID = 403110192802482730L;

      CheckBoxHeaderRenderer(JTableHeader header)
      {
         super();
         setHorizontalAlignment(JLabel.CENTER);
         setBorderPainted(true);
         setSelected(false);
         setOpaque(true);
         header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               setSelected(!isSelected());
               int selectedColumn = header.columnAtPoint(e.getPoint());
               if (selectedColumn == 0) {
                  JTable table = header.getTable();
                  int nrow = table.getRowCount();
                  if (nrow > 0) {
                     for (int i = 0; i < nrow; i++) {
                        if (isSelected()) {
                           table.setValueAt(true, i, 0);
                        }
                        else {
                           table.setValueAt(false, i, 0);
                        }
                     }
                  }
               }
            }
         });
      }

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
      {
         TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
         JLabel l = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
         l.setIcon(new ComponentIcon(this));
         return l;
      }

      private class ComponentIcon implements Icon
      {
         private final JCheckBox check;

         public ComponentIcon(JCheckBox check) {
             this.check = check;
         }

         @Override
         public int getIconWidth() {
             return check.getPreferredSize().width;
         }

         @Override
         public int getIconHeight() {
             return check.getPreferredSize().height;
         }

         @Override
         public void paintIcon(Component c, Graphics g, int x, int y) {
             SwingUtilities.paintComponent(g, check, (Container) c, x, y, getIconWidth(), getIconHeight());
         }
     }
   }

   /**
    * To allow user editing immediately the transformation rules by double-clicking the row.
    */
   class MappingExpressionSelectionListener extends MouseAdapter
   {
      private int lastSelectedRow = -1;

      @Override
      public void mouseClicked(MouseEvent e)
      {
         int selectedRow = tblTransformationRules.getSelectedRow();
         if (e.getClickCount() == 1) { // single click
            if (selectedRow != lastSelectedRow) {
               cmdEdit.setEnabled(true);
               cmdDelete.setEnabled(true);
               lastSelectedRow = selectedRow;
            } else {
               tblTransformationRules.clearSelection();
               cmdEdit.setEnabled(false);
               cmdDelete.setEnabled(false);
               lastSelectedRow = -1; // reset
            }
         } else if (e.getClickCount() == 2) { // double-click
            List<String> sheetNames = container.getActiveWorkbook().getSheetNames();
            TransformationRuleEditorPanel editorPanel = new TransformationRuleEditorPanel(sheetNames);
            editorPanel.fillFormFields(getValueAt(selectedRow, 1), getValueAt(selectedRow, 2),
                  getValueAt(selectedRow, 3), getValueAt(selectedRow, 4), getValueAt(selectedRow, 5),
                  getValueAt(selectedRow, 6), getValueAt(selectedRow, 7));
            showMappingEditorDialog(editorPanel, selectedRow);
         }
      }

      private String getValueAt(int row, int column)
      {
         return (String) tableModel.getValueAt(row, column);
      }
   }

   /*
    * Action listener implementations for command buttons
    */

   class AddButtonActionListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         List<String> sheetNames = container.getActiveWorkbook().getSheetNames();
         TransformationRuleEditorPanel editorPanel = new TransformationRuleEditorPanel(sheetNames);
         editorPanel.setSheetName(container.getActiveSheet().getName());
         editorPanel.setStartColumn(container.getActiveSheet().getSelectionRange().getStartColumnName());
         editorPanel.setStartRow(container.getActiveSheet().getSelectionRange().getStartRowNumber());
         editorPanel.setEndColumn(container.getActiveSheet().getSelectionRange().getEndColumnName());
         editorPanel.setEndRow(container.getActiveSheet().getSelectionRange().getEndRowNumber());
         showMappingEditorDialog(editorPanel, -1);
         fireTransformationRuleChange();
      }
   }

   class EditButtonActionListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         int selectedRow = tblTransformationRules.getSelectedRow();
         try {
            validateSelection(selectedRow);
            List<String> sheetNames = container.getActiveWorkbook().getSheetNames();
            TransformationRuleEditorPanel editorPanel = new TransformationRuleEditorPanel(sheetNames);
            editorPanel.fillFormFields(getValueAt(selectedRow, 1), getValueAt(selectedRow, 2),
                  getValueAt(selectedRow, 3), getValueAt(selectedRow, 4), getValueAt(selectedRow, 5),
                  getValueAt(selectedRow, 6), getValueAt(selectedRow, 7));
            showMappingEditorDialog(editorPanel, selectedRow);
         } catch (CellfieException ex) {
            getApplicationDialogManager().showMessageDialog(container, ex.getMessage());
         }
      }

      private String getValueAt(int row, int column)
      {
         return (String) tableModel.getValueAt(row, column);
      }
   }

   private void showMappingEditorDialog(TransformationRuleEditorPanel editorPanel, int selectedRow)
   {
      int answer = JOptionPaneEx.showConfirmDialog(
            container, "Transformation Rule Editor", editorPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
      switch (answer) {
         case JOptionPane.OK_OPTION :
            TransformationRule userInput = editorPanel.getUserInput();
            updateTableModel(selectedRow, userInput.getSheetName(), userInput.getStartColumn(),
                  userInput.getEndColumn(), userInput.getStartRow(), userInput.getEndRow(),
                  userInput.getRuleString(), userInput.getComment());
            cmdSaveAs.setEnabled(true);
            setPreferredColumnHeight();
            break;
      }
   }

   class DeleteRuleAction extends AbstractAction
   {
      private static final long serialVersionUID = 1L;
      
      @Override
      public void actionPerformed(ActionEvent e)
      {
         removeTransformationRule();
         fireTransformationRuleChange();
      }
   }

   class DeleteButtonActionListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         removeTransformationRule();
         fireTransformationRuleChange();
      }
   }

   private void removeTransformationRule()
   {
      int selectedRow = tblTransformationRules.getSelectedRow();
      try {
         validateSelection(selectedRow);
         int answer = getApplicationDialogManager().showConfirmDialog(container, "Confirm Delete",
               "Do you really want to delete the selected transformation rule?");
         switch (answer) {
            case JOptionPane.YES_OPTION :
               tableModel.removeRow(selectedRow);
               tblTransformationRules.setRowSelectionInterval(selectedRow, selectedRow);
         }
      } catch (CellfieException ex) {
         getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
      } catch (IllegalArgumentException ex) {
         resolveNextRowSelection(selectedRow);
      }
   }

   private void resolveNextRowSelection(int selectedRow)
   {
      try {
         // Select the previous row if there is no next row
         int previousRow = selectedRow - 1;
         tblTransformationRules.setRowSelectionInterval(previousRow, previousRow);
      } catch (IllegalArgumentException ex2) {
         // Clear the selection if the table has become empty
         tblTransformationRules.clearSelection();
         cmdEdit.setEnabled(false);
         cmdDelete.setEnabled(false);
      }
   }

   class OpenMappingAction implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         safeGuardChanges();
         try {
            File file = getApplicationDialogManager().showOpenFileChooser(
                  container, "Open Transformation Rule File", "json", "Transformation Rule File (.json)");
            if (file != null) {
               String filePath = file.getAbsolutePath();
               container.loadTransformationRuleDocument(filePath);
               fireTransformationRuleChange();
            }
         } catch (Exception ex) {
            getApplicationDialogManager().showErrorMessageDialog(container, "Error opening file: " + ex.getMessage());
            ex.printStackTrace();
         }
      }
   }

   private void validateSelection(int selectedRow) throws CellfieException
   {
      if (selectedRow == -1) {
         throw new CellfieException("No transformation rule was selected");
      }
   }

   public void fireTransformationRuleChange()
   {
      if (tblTransformationRules.getRowCount() > 0) {
         if (container.getRuleFileLocation().isPresent()) {
            cmdSave.setEnabled(true);
         }
         cmdSaveAs.setEnabled(true);
         cmdGenerateAxioms.setEnabled(true);
      } else {
         cmdGenerateAxioms.setEnabled(false);
      }
   }

   class SaveMappingAction implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         doSave(container.getRuleFileLocation().get());
      }
   }

   class SaveAsMappingAction implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         if (doSelectFileAndSave()) {
            enableSaveButton();
            updateBorderUI();
         }
      }
   }

   public boolean doSave(String filePath)
   {
      boolean isSuccessful = true;
      try {
         TransformationRuleSetFactory.saveTransformationRulesToDocument(filePath, tableModel.getTransformationRulesAndSave());
         container.updateTransformationRuleModel();
      } catch (IOException e) {
         isSuccessful = false;
         getApplicationDialogManager().showErrorMessageDialog(container, "Error saving file: " + e.getMessage());
      }
      return isSuccessful;
   }

   public boolean doSelectFileAndSave()
   {
      boolean isSuccessful = true;
      File file = getApplicationDialogManager().showSaveFileChooser(container, "Save As", "json", "Transformation Rule File (.json)", true);
      if (file != null) {
         String filePath = file.getAbsolutePath();
         String ext = ".json";
         if (!filePath.endsWith(ext)) {
            filePath = filePath + ext;
         }
         container.setRuleFileLocation(filePath);
         isSuccessful = doSave(filePath);
      } else {
         isSuccessful = false;
      }
      return isSuccessful;
   }

   public boolean safeGuardChanges()
   {
      boolean isSuccessful = true;
      if (tableModel.hasUnsavedChanges()) {
         int answer = JOptionPane.showConfirmDialog(
               container, "There are unsaved changes in your transformation rules. Do you want to save them?", 
               "Closing Cellfie", JOptionPane.YES_NO_CANCEL_OPTION);
         switch (answer) {
            case JOptionPane.YES_OPTION:
               Optional<String> fileLocation = container.getRuleFileLocation();
               if (!fileLocation.isPresent()) {
                  isSuccessful = doSelectFileAndSave();
               } else {
                  isSuccessful = doSave(fileLocation.get());
               }
               if (isSuccessful) {
                  getApplicationDialogManager().showMessageDialog(container, "Transformation rules saved successfully");
               }
               break;
            case JOptionPane.CANCEL_OPTION:
               isSuccessful = false; // avoid closing
               break;
         }
      }
      return isSuccessful;
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
         return option - o.option;
      }
   }
}
