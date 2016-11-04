package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;

import org.mm.workbook.Sheet;
import org.mm.workbook.WorkbookUtils;

/**
 * Represents the table model used to presenting the cells of a spreadsheet.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class SheetTableModel extends AbstractTableModel {

   private static final long serialVersionUID = 1L;

   private final Sheet sheet;

   public SheetTableModel(@Nonnull Sheet sheet) {
      this.sheet = checkNotNull(sheet);
   }

   @Override
   public int getRowCount() {
      return sheet.getLastRowIndex();
   }

   @Override
   public int getColumnCount() {
      int maxCount = 0;
      for (int currentRow = 0; currentRow < getRowCount(); currentRow++) {
         int currentCount = sheet.getLastColumnIndexAt(currentRow);
         if (currentCount > maxCount) {
            maxCount = currentCount;
         }
      }
      return maxCount;
   }

   @Override
   public String getColumnName(int column) {
      return WorkbookUtils.columnNumber2Name(column + 1);
   }

   @Override
   public Object getValueAt(int row, int column) {
      return sheet.getCellValue(row, column);
   }
}