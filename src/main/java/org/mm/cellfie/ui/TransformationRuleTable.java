package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mm.core.TransformationRule;

/**
 * Represents the table used to show the list of transformation rules.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleTable extends JTable {

   private static final long serialVersionUID = 1L;

   private final CheckBoxHeaderRenderer checkBoxRenderer = new CheckBoxHeaderRenderer(this);

   private TransformationRuleTableModel tableModel = TransformationRuleTableModel.createEmpty();

   public TransformationRuleTable() {
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setGridColor(new Color(220, 220, 220));
      setDefaultRenderer(String.class, new TransformationRuleTableRenderer());
      setModel(tableModel);
      setTableHeaderAlignment(SwingConstants.CENTER);
      getColumnModel().getColumn(0).setHeaderRenderer(checkBoxRenderer);
      setPreferredColumnWidth();
   }

   public void setContent(@Nonnull List<TransformationRule> transformationRules,
         @Nonnull TableModelListener listener) {
      checkNotNull(transformationRules);
      checkNotNull(listener);
      tableModel = TransformationRuleTableModel.create(transformationRules);
      tableModel.addTableModelListener(listener);
      setModel(tableModel);
      setTableHeaderAlignment(SwingConstants.CENTER);
      getColumnModel().getColumn(0).setHeaderRenderer(checkBoxRenderer);
      setPreferredColumnWidth();
      setPreferredRowHeight();
   }

   private void setTableHeaderAlignment(int alignment) {
      ((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(alignment);
   }

   public List<TransformationRule> getAllRules() {
      return tableModel.getAllRules();
   }

   public List<TransformationRule> getSelectedRules() {
      return tableModel.getSelectedRules();
   }

   public TransformationRule getRuleAt(int rowIndex) {
      return tableModel.getRuleAt(rowIndex);
   }

   public TransformationRule getRuleAtSelection() {
      return getRuleAt(getSelectedRow());
   }

   public void modifyRule(int selectedRow, String sheetName, String startColumn, String endColumn,
         String startRow, String endRow, String expression, String comment) {
      final Vector<Object> ruleRecord = createRuleRecordVector(sheetName, startColumn, endColumn,
            startRow, endRow, expression, comment);
      tableModel.removeRow(selectedRow);
      tableModel.insertRow(selectedRow, ruleRecord);
      repaintSelectionAfterModifying(selectedRow);
      setPreferredRowHeight();
   }

   public void modifyRuleAtSelection(String sheetName, String startColumn, String endColumn,
         String startRow, String endRow, String expression, String comment) {
      modifyRule(getSelectedRow(),
            sheetName,
            startColumn,
            endColumn,
            startRow, endRow,
            expression,
            comment);
   }

   private void repaintSelectionAfterModifying(int modifiedRow) {
      setRowSelectionInterval(modifiedRow, modifiedRow);
   }

   public void addRule(String sheetName, String startColumn, String endColumn, String startRow,
         String endRow, String expression, String comment) {
      final Vector<Object> ruleRecord = createRuleRecordVector(sheetName, startColumn, endColumn,
            startRow, endRow, expression, comment);
      tableModel.addRow(ruleRecord);
      repaintSelectionAfterAdding();
      setPreferredRowHeight();
   }

   private void repaintSelectionAfterAdding() {
      int addedRow = tableModel.getRowCount() - 1; // 0-index
      setRowSelectionInterval(addedRow, addedRow);
   }

   public void removeRule(int selectedRow) {
      tableModel.removeRow(selectedRow);
      repaintSelectionAfterDeleting(selectedRow);
   }

   public void removeRuleAtSelection() {
      removeRule(getSelectedRow());
   }

   private void repaintSelectionAfterDeleting(int deletedRow) {
      int nextSelectedRow = deletedRow;
      if (isLastRow(deletedRow)) {
         nextSelectedRow = deletedRow - 1; // the previous row becomes the next selected row
         if (!isRowPresent(nextSelectedRow)) {
            return; // skip drawing the row selection
         }
      }
      setRowSelectionInterval(nextSelectedRow, nextSelectedRow);
   }

   private boolean isLastRow(int deletedRow) {
      return deletedRow == tableModel.getAllRules().size();
   }

   private boolean isRowPresent(int nextSelectedRow) {
      return nextSelectedRow >= 0;
   }

   public void checkSelectAllRules() {
      checkBoxRenderer.setSelected(true);
   }

   public void uncheckSelectAllRules() {
      checkBoxRenderer.setSelected(false);
   }

   private void setPreferredColumnWidth() {
      final TableColumnModel columnModel = getColumnModel();
      columnModel.getColumn(TransformationRuleTableModel.RULE_SELECT_COLUMN).setPreferredWidth(30);
      columnModel.getColumn(TransformationRuleTableModel.RULE_SELECT_COLUMN).setMaxWidth(60);
      columnModel.getColumn(TransformationRuleTableModel.SHEET_NAME_COLUMN).setPreferredWidth(100);
      columnModel.getColumn(TransformationRuleTableModel.SHEET_NAME_COLUMN).setMaxWidth(120);
      columnModel.getColumn(TransformationRuleTableModel.START_COLUMN_COLUMN).setPreferredWidth(100);
      columnModel.getColumn(TransformationRuleTableModel.START_COLUMN_COLUMN).setMaxWidth(120);
      columnModel.getColumn(TransformationRuleTableModel.END_COLUMN_COLUMN).setPreferredWidth(100);
      columnModel.getColumn(TransformationRuleTableModel.END_COLUMN_COLUMN).setMaxWidth(120);
      columnModel.getColumn(TransformationRuleTableModel.START_ROW_COLUMN).setPreferredWidth(100);
      columnModel.getColumn(TransformationRuleTableModel.START_ROW_COLUMN).setMaxWidth(120);
      columnModel.getColumn(TransformationRuleTableModel.END_ROW_COLUMN).setPreferredWidth(100);
      columnModel.getColumn(TransformationRuleTableModel.END_ROW_COLUMN).setMaxWidth(120);
      columnModel.getColumn(TransformationRuleTableModel.RULE_EXPRESSION_COLUMN).setPreferredWidth(360);
      columnModel.getColumn(TransformationRuleTableModel.COMMENT_COLUMN).setPreferredWidth(180);
   }

   private void setPreferredRowHeight() {
      for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
         Object cellValue = tableModel.getValueAt(rowIndex, TransformationRuleTableModel.RULE_EXPRESSION_COLUMN);
         TableCellRenderer renderer = getDefaultRenderer(String.class);
         Component ruleExpressionField = renderer.getTableCellRendererComponent(
               this, cellValue, false, false,
               rowIndex, TransformationRuleTableModel.RULE_EXPRESSION_COLUMN);
         int preferredHeight = ruleExpressionField.getPreferredSize().height;
         setRowHeight(rowIndex, preferredHeight);
      }
   }

   private Vector<Object> createRuleRecordVector(String sheetName, String startColumn,
         String endColumn, String startRow, String endRow, String expression, String comment) {
      Vector<Object> row = new Vector<>();
      row.add(TransformationRuleTableModel.RULE_SELECT_COLUMN, true);
      row.add(TransformationRuleTableModel.SHEET_NAME_COLUMN, sheetName);
      row.add(TransformationRuleTableModel.START_COLUMN_COLUMN, startColumn);
      row.add(TransformationRuleTableModel.END_COLUMN_COLUMN, endColumn);
      row.add(TransformationRuleTableModel.START_ROW_COLUMN, startRow);
      row.add(TransformationRuleTableModel.END_ROW_COLUMN, endRow);
      row.add(TransformationRuleTableModel.RULE_EXPRESSION_COLUMN, expression);
      row.add(TransformationRuleTableModel.COMMENT_COLUMN, comment);
      return row;
   }
}
