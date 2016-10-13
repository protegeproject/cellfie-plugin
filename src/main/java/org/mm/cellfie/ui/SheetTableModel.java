package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.mm.ss.SpreadSheetUtil;

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
      if (sheet.rowIterator().hasNext()) {
         return sheet.getLastRowNum() + 1;
      } else {
         return 0; // is empty
      }
   }

   @Override
   public int getColumnCount() {
      int maxCount = 0;
      for (int i = 0; i < getRowCount(); i++) {
         Row row = sheet.getRow(i);
         if (row != null) {
            int currentCount = row.getLastCellNum();
            if (currentCount > maxCount) {
               maxCount = currentCount;
            }
         }
      }
      return maxCount;
   }

   @Override
   public String getColumnName(int column) {
      return SpreadSheetUtil.columnNumber2Name(column + 1);
   }

   @Override
   public Object getValueAt(int row, int column) {
      try {
         Cell cell = sheet.getRow(row).getCell(column);
         switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK :
               return "";
            case Cell.CELL_TYPE_STRING :
               return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC :
               // Check if the numeric is double or integer
               if (isInteger(cell.getNumericCellValue())) {
                  return (int) cell.getNumericCellValue();
               } else {
                  return cell.getNumericCellValue();
               }
            case Cell.CELL_TYPE_BOOLEAN :
               return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_FORMULA :
               return cell.getNumericCellValue();
            default :
               return "";
         }
      } catch (NullPointerException e) {
         // TODO: Add logger.warn
         return "";
      }
   }

   private boolean isInteger(double number) {
      return (number == Math.floor(number) && !Double.isInfinite(number));
   }
}