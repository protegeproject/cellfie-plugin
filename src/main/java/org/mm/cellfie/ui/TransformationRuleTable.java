package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

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

   private final TransformationRuleTableModel tableModel = new TransformationRuleTableModel();

   public TransformationRuleTable() {
      setModel(tableModel);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setGridColor(new Color(220, 220, 220));
      setDefaultRenderer(String.class, new TransformationRuleTableRenderer());
      setTableHeaderAlignment(SwingConstants.CENTER);
      getColumnModel().getColumn(0).setHeaderRenderer(checkBoxRenderer);
      setPreferredColumnWidth();
   }

   private void setTableHeaderAlignment(int alignment) {
      ((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(alignment);
   }

   public void addTableModelListener(TableModelListener listener) {
      tableModel.addTableModelListener(listener);
   }

   public void load(@Nonnull List<TransformationRule> rules) {
      checkNotNull(rules);
      for (TransformationRule rule : rules) {
         tableModel.addRule(rule);
         setPreferredRowHeight();
      }
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

   public void modifyRule(int selectedRow, @Nonnull TransformationRule rule) {
      checkNotNull(rule);
      tableModel.updateRule(selectedRow, rule);
      repaintSelectionAfterModifying(selectedRow);
      setPreferredRowHeight();
   }

   public void modifyRuleAtSelection(@Nonnull TransformationRule rule) {
      modifyRule(getSelectedRow(), rule);
   }

   private void repaintSelectionAfterModifying(int modifiedRow) {
      setRowSelectionInterval(modifiedRow, modifiedRow);
   }

   public void addRule(@Nonnull TransformationRule rule) {
      checkNotNull(rule);
      tableModel.addRule(rule);
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
}
