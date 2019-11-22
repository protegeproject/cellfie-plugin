package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.Color;
import java.awt.Component;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.mm.cellfie.transformationrule.TransformationRule;
import org.mm.cellfie.transformationrule.TransformationRuleSet;

/**
 * Represents the table used to show the list of transformation rules.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleTable extends JTable {

   private static final long serialVersionUID = 1L;

   private final CheckBoxHeaderRenderer checkBoxRenderer = new CheckBoxHeaderRenderer(this);

   private final TransformationRuleTableModel tableModel;

   public TransformationRuleTable(@Nonnull TransformationRuleTableModel tableModel) {
      this.tableModel = checkNotNull(tableModel);
      createGui();
   }

   private void createGui() {
      setModel(tableModel);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setGridColor(new Color(220, 220, 220));
      setDefaultRenderer(String.class, new TransformationRuleTableRenderer());
      setTableHeaderAlignment(SwingConstants.CENTER);
      setTableColumnLayout();
   }

   private void setTableHeaderAlignment(int alignment) {
      ((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(alignment);
   }

   @Override
   public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
      Component c = super.prepareRenderer(renderer, row, column);
      if (c instanceof JComponent) {
         JComponent jc = (JComponent) c;
         if(column == TransformationRuleTableModel.RULE_EXPRESSION_COLUMN){
            final TransformationRule tr = getRuleAt(row);
            String text = String.format(
                  "<html><b>Sheet name</b>: %s<br>"
                  + "<b>Cell range</b>: %s<br>"
                  + "<b>Desctiption</b>: %s</html>",
                  tr.getSheetName(), tr.getCellRange(), tr.getComment());
            jc.setToolTipText(text);
         }
      }
      return c;
  }

   public void load(@Nonnull TransformationRuleSet ruleSet) {
      checkNotNull(ruleSet);
      tableModel.removeAllRules();
      for (TransformationRule rule : ruleSet) {
         tableModel.addRule(rule);
         setPreferredRowHeight();
      }
   }

   public TransformationRuleSet getAllRules() {
      return TransformationRuleSet.create(tableModel.getAllRules());
   }

   public TransformationRuleSet getPickedRules() {
      return TransformationRuleSet.create(tableModel.getPickedRules());
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
      return deletedRow == tableModel.getRowCount();
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

   private void setTableColumnLayout() {
      final TableColumnModel columnModel = getColumnModel();
      columnModel.getColumn(TransformationRuleTableModel.RULE_PICK_COLUMN).setHeaderRenderer(checkBoxRenderer);
      columnModel.getColumn(TransformationRuleTableModel.RULE_PICK_COLUMN).setPreferredWidth(30);
      columnModel.getColumn(TransformationRuleTableModel.RULE_PICK_COLUMN).setMaxWidth(30);
      // Remove columns in the reverse order to not break the index ordering
      columnModel.removeColumn(columnModel.getColumn(TransformationRuleTableModel.DESCRIPTION_COLUMN));
      columnModel.removeColumn(columnModel.getColumn(TransformationRuleTableModel.END_ROW_COLUMN));
      columnModel.removeColumn(columnModel.getColumn(TransformationRuleTableModel.START_ROW_COLUMN));
      columnModel.removeColumn(columnModel.getColumn(TransformationRuleTableModel.END_COLUMN_COLUMN));
      columnModel.removeColumn(columnModel.getColumn(TransformationRuleTableModel.START_COLUMN_COLUMN));
      columnModel.removeColumn(columnModel.getColumn(TransformationRuleTableModel.SHEET_NAME_COLUMN));
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
