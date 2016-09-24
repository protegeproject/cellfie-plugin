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

   private static final int START_INDEX = 0;
   private static final int END_INDEX = -1;

   private final Sheet sheet;
   private final SheetTableModel sheetModel;

   private final SheetTable tblBaseSheet;
   private final JTableHeader header;
   private final JTable tblRowNumberSheet;

   private int startColumnIndex = START_INDEX; // initial range selection A:A
   private int startRowIndex = START_INDEX;
   private int endColumnIndex = START_INDEX;
   private int endRowIndex = END_INDEX;

   private boolean isSelectingRowHeaders = false;
   private boolean isSelectingColumnHeaders = false;

   private Point startMousePt;

   public SheetPanel(Sheet sheet)
   {
      this.sheet = sheet;
      sheetModel = new SheetTableModel(sheet);

      setLayout(new BorderLayout());

      tblBaseSheet = new SheetTable(sheetModel);
      tblBaseSheet.setCellSelectionEnabled(true);
      tblBaseSheet.addMouseListener(new SelectCellRange());

      header = tblBaseSheet.getTableHeader();
      header.setReorderingAllowed(false);
      header.addMouseListener(new SelectSingleColumnHeader());
      header.addMouseMotionListener(new SelectMultipleColumnHeaders());

      tblRowNumberSheet = new RowNumberWrapper(tblBaseSheet);
      tblRowNumberSheet.addMouseListener(new SelectSingleRowHeader());
      tblRowNumberSheet.addMouseMotionListener(new SelectMultipleRowHeaders());

      JScrollPane scrBaseSheet = new JScrollPane(tblBaseSheet);
      scrBaseSheet.setRowHeaderView(tblRowNumberSheet);
      scrBaseSheet.setCorner(JScrollPane.UPPER_LEFT_CORNER, tblRowNumberSheet.getTableHeader());

      add(BorderLayout.CENTER, scrBaseSheet);

      validate();
   }

   public String getSheetName()
   {
      return sheet.getSheetName();
   }

   private void setSelectionRange(int startColumnIndex, int startRowIndex, int endColumnIndex, int endRowIndex)
   {
      this.startColumnIndex = startColumnIndex;
      this.startRowIndex = startRowIndex;
      this.endColumnIndex = endColumnIndex;
      this.endRowIndex = endRowIndex;
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

   /**
    * Mouse adapter for selecting a cell range in the spreadsheet
    */
   class SelectCellRange extends MouseAdapter {
      @Override
      public void mouseReleased(MouseEvent e) {
         if (isControlKeyPressed(e)) {
            selectCellRangeOnKeyModifier(e);
         }
         else {
            selectCellRangeOnMouseDragged();
         }
      }

      private void selectCellRangeOnKeyModifier(MouseEvent e) {
         Point deselectPoint = e.getPoint();
         int[] selectedColumnRange = new int[] { startColumnIndex, endColumnIndex };
         int[] selectedRowRange = new int[] { startRowIndex, endRowIndex };
         recomputeColumnRangeSelection(deselectPoint, selectedColumnRange);
         recomputeRowRangeSelection(deselectPoint, selectedRowRange);
         setSelectionRange(selectedColumnRange[0], selectedRowRange[0], selectedColumnRange[1], selectedRowRange[1]);
         drawCellSelection(selectedColumnRange[0], selectedRowRange[0], selectedColumnRange[1], selectedRowRange[1]);
      }

      private void selectCellRangeOnMouseDragged() {
         int[] selectedColumns = tblBaseSheet.getSelectedColumns();
         int[] selectedRows = tblBaseSheet.getSelectedRows();
         if (selectedColumns.length > 1 || selectedRows.length > 1) {
            setSelectionRange(selectedColumns[0], selectedRows[0],
                  selectedColumns[selectedColumns.length-1],
                  selectedRows[selectedRows.length-1]);
         }
         else {
            setSelectionRange(START_INDEX, START_INDEX, START_INDEX, END_INDEX); // A:A
         }
         resetSelectionType();
      }

      private void recomputeRowRangeSelection(Point deselectPoint, int[] selectedRowRange) {
         if (isSelectingColumnHeaders) {
            int deselectedRowIndex = tblBaseSheet.rowAtPoint(deselectPoint);
            int selectedRowIndex = deselectedRowIndex + 1; // the new selection is the next row index
            int maxRowIndex = tblBaseSheet.getRowCount() - 1; // 0-index
            if (selectedRowIndex > maxRowIndex) {
               selectedRowIndex = maxRowIndex;
            }
            selectedRowRange[0] = selectedRowIndex;
         }
      }

      private void recomputeColumnRangeSelection(Point deselectPoint, int[] selectedColumnRange) {
         if (isSelectingRowHeaders) {
            int deselectedColumnIndex =  tblBaseSheet.columnAtPoint(deselectPoint);
            int selectedColumnIndex = deselectedColumnIndex + 1; // the new selection is the next column index
            int maxColumnIndex = tblBaseSheet.getColumnCount() - 1; // 0-index
            if (selectedColumnIndex > maxColumnIndex) {
               selectedColumnIndex = maxColumnIndex;
            }
            selectedColumnRange[0] = selectedColumnIndex;
         }
      }

      private boolean isControlKeyPressed(MouseEvent e) {
         if (System.getProperty("os.name").contains("Mac OS X")) {
            return e.isMetaDown();
         }
         else {
            return e.isControlDown();
         }
      }

      private void resetSelectionType() {
         isSelectingRowHeaders = false;
         isSelectingColumnHeaders = false;
      }
   }

   /**
    * Mouse adapter for selecting a single column header in the spreadsheet.
    * The selection causes all rows in that column will be highlighted.
    */
   class SelectSingleColumnHeader extends MouseAdapter {
      @Override
      public void mousePressed(MouseEvent e) {
         startMousePt = e.getPoint(); // set the initial clicking
         int columnIndexAtSelection = header.columnAtPoint(startMousePt);
         drawCellSelection(columnIndexAtSelection, START_INDEX, columnIndexAtSelection, END_INDEX);
      }
      
      @Override
      public void mouseReleased(MouseEvent e) {
         int[] selectedColumns = tblBaseSheet.getSelectedColumns();
         int startColumnIndex = selectedColumns[0];
         int endColumnIndex = selectedColumns[selectedColumns.length - 1];
         setSelectionRange(startColumnIndex, START_INDEX, endColumnIndex, END_INDEX);
      }
   }

   /**
    * Mouse adapter for selecting multiple column headers in the spreadsheet.
    * The selection causes all rows in those columns will be highlighted.
    */
   class SelectMultipleColumnHeaders extends MouseAdapter {
      @Override
      public void mouseDragged(MouseEvent e) {
         int columnIndexAtInitialSelection = header.columnAtPoint(startMousePt);
         int columnIndexAtCurrentSelection = header.columnAtPoint(e.getPoint());
         drawCellSelection(columnIndexAtInitialSelection, START_INDEX, columnIndexAtCurrentSelection, END_INDEX);
         markSelectionType();
      }
      
      private void markSelectionType() {
         isSelectingRowHeaders = false;
         isSelectingColumnHeaders = true;
      }
   }

   /**
    * Mouse adapter for selecting a single row header in the spreadsheet.
    * The selection causes all columns in that row will be highlighted.
    */
   class SelectSingleRowHeader extends MouseAdapter {
      @Override
      public void mousePressed(MouseEvent e) {
         startMousePt = e.getPoint(); // set the initial clicking
         int rowIndexAtSelection = tblRowNumberSheet.rowAtPoint(startMousePt);
         drawCellSelection(START_INDEX, rowIndexAtSelection, END_INDEX, rowIndexAtSelection);
      }
      
      @Override
      public void mouseReleased(MouseEvent e) {
         int[] selectedRows = tblBaseSheet.getSelectedRows();
         int startRowIndex = selectedRows[0];
         int endRowIndex = selectedRows[selectedRows.length - 1];
         setSelectionRange(START_INDEX, startRowIndex, END_INDEX, endRowIndex);
      }
   }

   /**
    * Mouse adapter for selecting multiple row headers in the spreadsheet.
    * The selection causes all rows in those rows will be highlighted.
    */
   class SelectMultipleRowHeaders extends MouseAdapter {
      @Override
      public void mouseDragged(MouseEvent e) {
         int rowIndexAtInitialSelection = tblRowNumberSheet.rowAtPoint(startMousePt);
         int rowIndexAtCurrentSelection = tblRowNumberSheet.rowAtPoint(e.getPoint());
         drawCellSelection(START_INDEX, rowIndexAtInitialSelection, END_INDEX, rowIndexAtCurrentSelection);
         markSelectionType();
      }
      
      private void markSelectionType() {
         isSelectingRowHeaders = true;
         isSelectingColumnHeaders = false;
      }
   }

   private void drawCellSelection(int startColumnIndex, int startRowIndex, int endColumnIndex, int endRowIndex) {
      tblBaseSheet.setColumnSelectionInterval(startColumnIndex,
            (endColumnIndex == END_INDEX ? tblBaseSheet.getColumnCount() - 1 : endColumnIndex));
      tblBaseSheet.setRowSelectionInterval(startRowIndex,
            (endRowIndex == END_INDEX ? tblBaseSheet.getRowCount() - 1 : endRowIndex));
   }
}
