package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.mm.ss.SpreadSheetUtil;

public class SheetPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private final Sheet sheet;
   private final SheetTableModel sheetModel;

   public SheetPanel(Sheet sheet)
   {
      this.sheet = sheet;
      sheetModel = new SheetTableModel(sheet);

      setLayout(new BorderLayout());

      SheetTable tblBaseSheet = new SheetTable(sheetModel);
      JScrollPane scrBaseSheet = new JScrollPane(tblBaseSheet);

      JTable tblRowNumberSheet = new RowNumberWrapper(tblBaseSheet);
      scrBaseSheet.setRowHeaderView(tblRowNumberSheet);
      scrBaseSheet.setCorner(JScrollPane.UPPER_LEFT_CORNER, tblRowNumberSheet.getTableHeader());

      add(BorderLayout.CENTER, scrBaseSheet);

      validate();
   }

   public String getSheetName()
   {
      return sheet.getSheetName();
   }

   class SheetTableModel extends AbstractTableModel
   {
      private static final long serialVersionUID = 1L;

      private final Sheet sheet;

      public SheetTableModel(Sheet sheet)
      {
         this.sheet = sheet;
      }

      public int getRowCount()
      {
         return sheet.getLastRowNum() + 1;
      }

      public int getColumnCount()
      {
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

      public String getColumnName(int column)
      {
         return SpreadSheetUtil.columnNumber2Name(column + 1);
      }

      public Object getValueAt(int row, int column)
      {
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
            // TODO Log this strange error
            return "";
         }
      }

      private boolean isInteger(double number)
      {
         return (number == Math.floor(number) && !Double.isInfinite(number));
      }
   }
}
