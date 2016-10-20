package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.swing.table.DefaultTableModel;

import org.mm.core.TransformationRule;

/**
 * A helper class used for data transaction between the table UI and the table model.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleTableModelHelper {

   private final DefaultTableModel tableModel;

   public TransformationRuleTableModelHelper(@Nonnull DefaultTableModel tableModel) {
      this.tableModel = checkNotNull(tableModel);
   }

   public void addRule(@Nonnull TransformationRule rule) {
      tableModel.addRow(asRowVector(rule));
   }

   @Nonnull
   public List<TransformationRule> getAllRules() {
      List<TransformationRule> rules = new ArrayList<>();
      for (int rowIndex = 0; rowIndex < tableModel.getRowCount(); rowIndex++) {
         rules.add(getRuleAt(rowIndex));
      }
      return rules;
   }

   @Nonnull
   public List<TransformationRule> getPickedRules() {
      List<TransformationRule> rules = new ArrayList<>();
      for (int rowIndex = 0; rowIndex < tableModel.getRowCount(); rowIndex++) {
         if (isRulePickedAt(rowIndex)) {
            rules.add(getRuleAt(rowIndex));
         }
      }
      return rules;
   }

   @Nonnull
   public TransformationRule getRuleAt(int rowIndex) {
      final Vector<?> rowVector = (Vector<?>) tableModel.getDataVector().elementAt(rowIndex);
      String sheetName = String.valueOf(rowVector.get(TransformationRuleTableModel.SHEET_NAME_COLUMN));
      String startColumn = String.valueOf(rowVector.get(TransformationRuleTableModel.START_COLUMN_COLUMN));
      String endColumn = String.valueOf(rowVector.get(TransformationRuleTableModel.END_COLUMN_COLUMN));
      String startRow = String.valueOf(rowVector.get(TransformationRuleTableModel.START_ROW_COLUMN));
      String endRow = String.valueOf(rowVector.get(TransformationRuleTableModel.END_ROW_COLUMN));
      String expression = String.valueOf(rowVector.get(TransformationRuleTableModel.RULE_EXPRESSION_COLUMN));
      String comment = String.valueOf(rowVector.get(TransformationRuleTableModel.COMMENT_COLUMN));
      return new TransformationRule(sheetName, startColumn, endColumn, startRow, endRow, comment, expression);
   }

   public void removeRule(int selectedRow) {
      tableModel.removeRow(selectedRow);
   }

   public void updateRule(int selectedRow, @Nonnull TransformationRule newRule) {
      checkNotNull(newRule);
      tableModel.removeRow(selectedRow);
      tableModel.insertRow(selectedRow, asRowVector(newRule));
   }

   private boolean isRulePickedAt(int rowIndex) {
      final Vector<?> rowVector = (Vector<?>) tableModel.getDataVector().elementAt(rowIndex);
      return (Boolean) rowVector.get(TransformationRuleTableModel.RULE_PICK_COLUMN);
   }

   private static Vector<Object> asRowVector(TransformationRule rule) {
      Vector<Object> rowVector = new Vector<>();
      rowVector.add(TransformationRuleTableModel.RULE_PICK_COLUMN, true);
      rowVector.add(TransformationRuleTableModel.SHEET_NAME_COLUMN, rule.getSheetName());
      rowVector.add(TransformationRuleTableModel.START_COLUMN_COLUMN, rule.getStartColumn());
      rowVector.add(TransformationRuleTableModel.END_COLUMN_COLUMN, rule.getEndColumn());
      rowVector.add(TransformationRuleTableModel.START_ROW_COLUMN, rule.getStartRow());
      rowVector.add(TransformationRuleTableModel.END_ROW_COLUMN, rule.getEndRow());
      rowVector.add(TransformationRuleTableModel.RULE_EXPRESSION_COLUMN, rule.getRuleExpression());
      rowVector.add(TransformationRuleTableModel.COMMENT_COLUMN, rule.getComment());
      return rowVector;
   }
}
