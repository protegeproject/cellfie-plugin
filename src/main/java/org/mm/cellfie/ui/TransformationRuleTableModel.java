package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.swing.table.DefaultTableModel;
import org.mm.cellfie.transformationrule.TransformationRule;

/**
 * Represents the table model used to organize the transformation rule details
 * in the table presentation
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleTableModel extends DefaultTableModel {

   private static final long serialVersionUID = 1L;

   public static final int RULE_PICK_COLUMN = 0;
   public static final int RULE_EXPRESSION_COLUMN = 1;
   public static final int SHEET_NAME_COLUMN = 2;
   public static final int START_COLUMN_COLUMN = 3;
   public static final int END_COLUMN_COLUMN = 4;
   public static final int START_ROW_COLUMN = 5;
   public static final int END_ROW_COLUMN = 6;
   public static final int DESCRIPTION_COLUMN = 7;

   private final String[] COLUMN_NAMES = { "", "Expression Rule",
         "Sheet Name", "Start Column", "End Column", "Start Row",
         "End Row", "Description" };

   public TransformationRuleTableModel() {
      super();
   }

   @Override
   public String getColumnName(int column) {
      return COLUMN_NAMES[column];
   }

   @Override
   public int getColumnCount() {
      return COLUMN_NAMES.length;
   }

   @Override
   public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == 0) {
         return Boolean.class;
      }
      return String.class;
   }

   @Override
   public boolean isCellEditable(int rowIndex, int columnIndex) {
      if (columnIndex == 0) {
         return true;
      }
      return false;
   }

   public void addRule(@Nonnull TransformationRule rule) {
      addRow(asRowVector(rule));
   }

   public void updateRule(int selectedRow, @Nonnull TransformationRule newRule) {
      checkNotNull(newRule);
      removeRow(selectedRow);
      insertRow(selectedRow, asRowVector(newRule));
   }

   public void removeRule(int selectedRow) {
      removeRow(selectedRow);
   }

   public void removeAllRules() {
      setRowCount(0);
   }

   @Nonnull
   public List<TransformationRule> getAllRules() {
      List<TransformationRule> rules = new ArrayList<>();
      for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
         rules.add(getRuleAt(rowIndex));
      }
      return rules;
   }

   @Nonnull
   public List<TransformationRule> getPickedRules() {
      List<TransformationRule> rules = new ArrayList<>();
      for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
         if (isRulePickedAt(rowIndex)) {
            rules.add(getRuleAt(rowIndex));
         }
      }
      return rules;
   }

   private boolean isRulePickedAt(int rowIndex) {
      final Vector<?> rowVector = (Vector<?>) getDataVector().elementAt(rowIndex);
      return (Boolean) rowVector.get(TransformationRuleTableModel.RULE_PICK_COLUMN);
   }

   @Nonnull
   public TransformationRule getRuleAt(int rowIndex) {
      final Vector<?> rowVector = (Vector<?>) getDataVector().elementAt(rowIndex);
      String sheetName = String.valueOf(rowVector.get(TransformationRuleTableModel.SHEET_NAME_COLUMN));
      String startColumn = String.valueOf(rowVector.get(TransformationRuleTableModel.START_COLUMN_COLUMN));
      String endColumn = String.valueOf(rowVector.get(TransformationRuleTableModel.END_COLUMN_COLUMN));
      String startRow = String.valueOf(rowVector.get(TransformationRuleTableModel.START_ROW_COLUMN));
      String endRow = String.valueOf(rowVector.get(TransformationRuleTableModel.END_ROW_COLUMN));
      String expression = String.valueOf(rowVector.get(TransformationRuleTableModel.RULE_EXPRESSION_COLUMN));
      String description = String.valueOf(rowVector.get(TransformationRuleTableModel.DESCRIPTION_COLUMN));
      return new TransformationRule(sheetName, startColumn, endColumn, startRow, endRow, description, expression);
   }

   private static Vector<Object> asRowVector(TransformationRule rule) {
      Vector<Object> rowVector = new Vector<>();
      rowVector.add(TransformationRuleTableModel.RULE_PICK_COLUMN, true);
      rowVector.add(TransformationRuleTableModel.RULE_EXPRESSION_COLUMN, rule.getRuleExpression());
      rowVector.add(TransformationRuleTableModel.SHEET_NAME_COLUMN, rule.getSheetName());
      rowVector.add(TransformationRuleTableModel.START_COLUMN_COLUMN, rule.getStartColumn());
      rowVector.add(TransformationRuleTableModel.END_COLUMN_COLUMN, rule.getEndColumn());
      rowVector.add(TransformationRuleTableModel.START_ROW_COLUMN, rule.getStartRow());
      rowVector.add(TransformationRuleTableModel.END_ROW_COLUMN, rule.getEndRow());
      rowVector.add(TransformationRuleTableModel.DESCRIPTION_COLUMN, rule.getComment());
      return rowVector;
   }
}
