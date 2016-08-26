package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.mm.ss.SpreadSheetUtil;

public class SheetPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private final Sheet sheet;
   private final SheetTableModel sheetModel;

   private final SheetTable tblBaseSheet;

   private int startColumnIndex = -1;
   private int startRowIndex = -1;
   private int endColumnIndex = -1;
   private int endRowIndex = -1;

   private Point startMousePt;

   public SheetPanel(Sheet sheet)
   {
      this.sheet = sheet;
      sheetModel = new SheetTableModel(sheet);

      setLayout(new BorderLayout());

      tblBaseSheet = new SheetTable(sheetModel);
      tblBaseSheet.setCellSelectionEnabled(true);
      tblBaseSheet.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseReleased(MouseEvent e) {
            int[] selectedColumns = tblBaseSheet.getSelectedColumns();
            int[] selectedRows = tblBaseSheet.getSelectedRows();
            if (selectedColumns.length > 0 || selectedRows.length > 0) {
               setSelectionRange(
                     selectedColumns[0],
                     selectedRows[0],
                     selectedColumns[selectedColumns.length-1],
                     selectedRows[selectedRows.length-1]);
            }
            else {
               setSelectionRange(0, 0, 0, -1); // A-A-1-+
            }
         }
      });
      JScrollPane scrBaseSheet = new JScrollPane(tblBaseSheet);

      JTableHeader header = tblBaseSheet.getTableHeader();
      header.setReorderingAllowed(false);
      header.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            startMousePt = e.getPoint();
            int col0 = header.columnAtPoint(startMousePt);
            tblBaseSheet.setColumnSelectionInterval(col0, col0);
            tblBaseSheet.setRowSelectionInterval(0, tblBaseSheet.getRowCount()-1); // 0-indexed
         }
         @Override
         public void mouseReleased(MouseEvent e) {
            int[] selectedColumns = tblBaseSheet.getSelectedColumns();
            setSelectionRange(
                  selectedColumns[0],
                  -1,
                  selectedColumns[selectedColumns.length-1],
                  -1);
         }
      });
      header.addMouseMotionListener(new MouseAdapter() {
         @Override
         public void mouseDragged(MouseEvent e) {
            int col0 = header.columnAtPoint(startMousePt);
            int col1 = header.columnAtPoint(e.getPoint());
            tblBaseSheet.setColumnSelectionInterval(col0, col1);
            tblBaseSheet.setRowSelectionInterval(0, tblBaseSheet.getRowCount()-1); // 0-indexed
         }
      });

      JTable tblRowNumberSheet = new RowNumberWrapper(tblBaseSheet);
      tblRowNumberSheet.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            startMousePt = e.getPoint();
            int row0 = tblRowNumberSheet.rowAtPoint(startMousePt);
            tblBaseSheet.setColumnSelectionInterval(0, tblBaseSheet.getColumnCount()-1); // 0-indexed
            tblBaseSheet.setRowSelectionInterval(row0, row0);
         }
         @Override
         public void mouseReleased(MouseEvent e) {
            int[] selectedRows = tblBaseSheet.getSelectedRows();
            setSelectionRange(
                  -1,
                  selectedRows[0],
                  -1,
                  selectedRows[selectedRows.length-1]);
         }
      });
      tblRowNumberSheet.addMouseMotionListener(new MouseAdapter() {
         @Override
         public void mouseDragged(MouseEvent e) {
            int row0 = tblRowNumberSheet.rowAtPoint(startMousePt);
            int row1 = tblRowNumberSheet.rowAtPoint(e.getPoint());
            tblBaseSheet.setColumnSelectionInterval(0, tblBaseSheet.getColumnCount()-1); // 0-indexed
            tblBaseSheet.setRowSelectionInterval(row0, row1);
         }
      });
      
      scrBaseSheet.setRowHeaderView(tblRowNumberSheet);
      scrBaseSheet.setCorner(JScrollPane.UPPER_LEFT_CORNER, tblRowNumberSheet.getTableHeader());

      add(BorderLayout.CENTER, scrBaseSheet);

      validate();
   }

   private void setSelectionRange(int startColumn, int startRow, int endColumn, int endRow)
   {
      startColumnIndex = startColumn;
      startRowIndex = startRow;
      endColumnIndex = endColumn;
      endRowIndex = endRow;
   }

   public String getSheetName()
   {
      return sheet.getSheetName();
   }

   public int[] getSelectionRange()
   {
      return new int[] {startColumnIndex, startRowIndex, endColumnIndex, endRowIndex};
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
         if (sheet.rowIterator().hasNext()) {
            return sheet.getLastRowNum() + 1;
         } else {
            return 0; // is empty
         }
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
