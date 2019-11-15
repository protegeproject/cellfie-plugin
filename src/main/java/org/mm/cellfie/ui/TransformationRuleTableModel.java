package org.mm.cellfie.ui;

import javax.swing.table.DefaultTableModel;

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
}
