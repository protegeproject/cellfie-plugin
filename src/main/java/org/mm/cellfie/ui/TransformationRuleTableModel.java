package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.swing.table.DefaultTableModel;

import org.mm.core.TransformationRule;

/**
 * Represents the table model used to organize the transformation rule details
 * in the table presentation
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleTableModel extends DefaultTableModel {

   private static final long serialVersionUID = 1L;

   public static final int RULE_SELECT_COLUMN = 0;
   public static final int SHEET_NAME_COLUMN = 1;
   public static final int START_COLUMN_COLUMN = 2;
   public static final int END_COLUMN_COLUMN = 3;
   public static final int START_ROW_COLUMN = 4;
   public static final int END_ROW_COLUMN = 5;
   public static final int RULE_EXPRESSION_COLUMN = 6;
   public static final int COMMENT_COLUMN = 7;

   private final String[] COLUMN_NAMES = { "", "Sheet Name", "Start Column", "End Column",
         "Start Row", "End Row", "Rule", "Comment" };

   // Prevent external instantiation
   private TransformationRuleTableModel(@Nonnull final List<TransformationRule> rules) {
      super();
      checkNotNull(rules);
      for (TransformationRule rule : rules) {
         Vector<Object> row = new Vector<>();
         row.add(true);
         row.add(rule.getSheetName());
         row.add(rule.getStartColumn());
         row.add(rule.getEndColumn());
         row.add(rule.getStartRow());
         row.add(rule.getEndRow());
         row.add(rule.getRuleString());
         row.add(rule.getComment());
         addRow(row);
      }
   }

   public static TransformationRuleTableModel create(@Nonnull final List<TransformationRule> rules) {
      return new TransformationRuleTableModel(rules);
   }

   public static TransformationRuleTableModel createEmpty() {
      return new TransformationRuleTableModel(Collections.emptyList());
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
      addRow(asDataVector(rule));
   }

   public List<TransformationRule> getAllRules() {
      List<TransformationRule> rules = new ArrayList<>();
      for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
         rules.add(getRuleAt(rowIndex));
      }
      return rules;
   }

   public List<TransformationRule> getSelectedRules() { // TODO: Rename to getCheckedRules
      List<TransformationRule> rules = new ArrayList<>();
      for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
         if (isRuleSelectedAt(rowIndex)) {
            rules.add(getRuleAt(rowIndex));
         }
      }
      return rules;
   }

   public void removeRule(int selectedRow) {
      removeRow(selectedRow);
   }

   public void updateRule(int selectedRow, TransformationRule rule) {
      removeRow(selectedRow);
      insertRow(selectedRow, asDataVector(rule));
   }

   public TransformationRule getRuleAt(int rowIndex) {
      final Vector<?> dataVector = (Vector<?>) getDataVector().elementAt(rowIndex);
      String sheetName = String.valueOf(dataVector.get(SHEET_NAME_COLUMN));
      String startColumn = String.valueOf(dataVector.get(START_COLUMN_COLUMN));
      String endColumn = String.valueOf(dataVector.get(END_COLUMN_COLUMN));
      String startRow = String.valueOf(dataVector.get(START_ROW_COLUMN));
      String endRow = String.valueOf(dataVector.get(END_ROW_COLUMN));
      String expression = String.valueOf(dataVector.get(RULE_EXPRESSION_COLUMN));
      String comment = String.valueOf(dataVector.get(COMMENT_COLUMN));
      return new TransformationRule(sheetName, startColumn, endColumn, startRow, endRow, comment, expression);
   }

   private boolean isRuleSelectedAt(int rowIndex) { // TODO: Rename to isRuleCheckedAt
      final Vector<?> rowVector = (Vector<?>) getDataVector().elementAt(rowIndex);
      return ((Boolean) rowVector.get(0)) == true;
   }

   private static Vector<Object> asDataVector(@Nonnull TransformationRule rule) {
      checkNotNull(rule);
      Vector<Object> dataVector = new Vector<>();
      dataVector.add(RULE_SELECT_COLUMN, true);
      dataVector.add(SHEET_NAME_COLUMN, rule.getSheetName());
      dataVector.add(START_COLUMN_COLUMN, rule.getStartColumn());
      dataVector.add(END_COLUMN_COLUMN, rule.getEndColumn());
      dataVector.add(START_ROW_COLUMN, rule.getStartRow());
      dataVector.add(END_ROW_COLUMN, rule.getEndRow());
      dataVector.add(RULE_EXPRESSION_COLUMN, rule.getRuleString());
      dataVector.add(COMMENT_COLUMN, rule.getComment());
      return dataVector;
   }
}
