package org.mm.cellfie.ui.view;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.mm.ss.SpreadSheetUtil;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class SheetPanel extends JPanel {

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

   private Point startMousePt;
   private Point endMousePt;

   /**
    * Constructs the UI panel for the given input {@code Sheet} object.
    *
    * @param sheet
    *           The Apache POI sheet instance
    */
   public SheetPanel(@Nonnull Sheet sheet) {
      this.sheet = checkNotNull(sheet);
      sheetModel = new SheetTableModel(sheet);

      setLayout(new BorderLayout());

      tblBaseSheet = new SheetTable(sheetModel);
      tblBaseSheet.setCellSelectionEnabled(true);
      tblBaseSheet.addMouseListener(new SelectCellRange());

      header = tblBaseSheet.getTableHeader();
      header.setReorderingAllowed(false);
      header.addMouseListener(new SelectColumnHeadersOnMouseClicked());
      header.addMouseMotionListener(new SelectColumnHeadersOnMouseDragged());

      tblRowNumberSheet = new RowNumberWrapper(tblBaseSheet);
      tblRowNumberSheet.addMouseListener(new SelectRowHeadersOnMouseClicked());
      tblRowNumberSheet.addMouseMotionListener(new SelectRowHeadersOnMouseDragged());

      JScrollPane scrBaseSheet = new JScrollPane(tblBaseSheet);
      scrBaseSheet.setRowHeaderView(tblRowNumberSheet);
      scrBaseSheet.setCorner(JScrollPane.UPPER_LEFT_CORNER, tblRowNumberSheet.getTableHeader());

      add(BorderLayout.CENTER, scrBaseSheet);

      validate();
   }

   /**
    * Returns the name of the sheet presented by this UI panel.
    *
    * @return The sheet name.
    */
   public String getSheetName() {
      return sheet.getSheetName();
   }

   private void setSelectionRange(int startColumnIndex, int startRowIndex, int endColumnIndex, int endRowIndex) {
      this.startColumnIndex = startColumnIndex;
      this.startRowIndex = startRowIndex;
      this.endColumnIndex = endColumnIndex;
      this.endRowIndex = endRowIndex;
   }

   /**
    * Returns the array of the cell selection from this UI panel. The array is composed by
    * { startColumnIndex, startRowIndex, endColumnIndex, endRowIndex }
    *
    * @return The selection array.
    */
   public int[] getSelectionRange() {
      return new int[] { startColumnIndex, startRowIndex, endColumnIndex, endRowIndex };
   }

   /**
    * The table model used to presenting the data from the Apache POI Sheet instance
    */
   class SheetTableModel extends AbstractTableModel {
      private static final long serialVersionUID = 1L;

      private final Sheet sheet;

      public SheetTableModel(@Nonnull Sheet sheet) {
         this.sheet = checkNotNull(sheet);
      }

      public int getRowCount() {
         if (sheet.rowIterator().hasNext()) {
            return sheet.getLastRowNum() + 1;
         } else {
            return 0; // is empty
         }
      }

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

      public String getColumnName(int column) {
         return SpreadSheetUtil.columnNumber2Name(column + 1);
      }

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
            // TODO Log this strange error
            return "";
         }
      }

      private boolean isInteger(double number) {
         return (number == Math.floor(number) && !Double.isInfinite(number));
      }
   }

   /**
    * Mouse adapter for selecting a cell range in the spreadsheet
    */
   class SelectCellRange extends MouseAdapter {
      @Override
      public void mouseReleased(MouseEvent e) {
         int[] selectedColumns = tblBaseSheet.getSelectedColumns();
         int[] selectedRows = tblBaseSheet.getSelectedRows();
         if (selectedColumns.length == 0 || selectedRows.length == 0) {
            setSelectionRange(START_INDEX, START_INDEX, START_INDEX, END_INDEX); // A:A
         } else {
            setSelectionRange(selectedColumns[0], selectedRows[0],
                  selectedColumns[selectedColumns.length - 1],
                  selectedRows[selectedRows.length - 1]);
         }
      }
   }

   /**
    * Mouse adapter for selecting the column header in the spreadsheet using a mouse
    * click or a mouse click with SHIFT key modifier.
    */
   class SelectColumnHeadersOnMouseClicked extends MouseAdapter {
      @Override
      public void mousePressed(MouseEvent e) {
         setMouseClickingPoint(e);
         int startColumnIndexAtSelection = header.columnAtPoint(startMousePt);
         int endColumnIndexAtSelection = header.columnAtPoint(endMousePt);
         drawCellSelection(startColumnIndexAtSelection, START_INDEX, endColumnIndexAtSelection, END_INDEX);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         int[] selectedColumns = tblBaseSheet.getSelectedColumns();
         int startColumnIndex = selectedColumns[0];
         int endColumnIndex = selectedColumns[selectedColumns.length - 1];
         setSelectionRange(startColumnIndex, START_INDEX, endColumnIndex, END_INDEX);
      }

      private void setMouseClickingPoint(MouseEvent e) {
         if (!isShiftKeyIsPressed(e)) {
            startMousePt = e.getPoint(); // set the initial clicking point, skip if SHIFT key is pressed
         }
         endMousePt = e.getPoint();
      }

      private boolean isShiftKeyIsPressed(MouseEvent e) {
         return e.isShiftDown();
      }
   }

   /**
    * Mouse adapter for selecting multiple column headers in the spreadsheet using
    * a mouse dragged gesture.
    */
   class SelectColumnHeadersOnMouseDragged extends MouseAdapter {
      @Override
      public void mouseDragged(MouseEvent e) {
         int columnIndexAtInitialSelection = header.columnAtPoint(startMousePt);
         int columnIndexAtCurrentSelection = header.columnAtPoint(e.getPoint());
         drawCellSelection(columnIndexAtInitialSelection, START_INDEX, columnIndexAtCurrentSelection, END_INDEX);
      }
   }

   /**
    * Mouse adapter for selecting the row header in the spreadsheet using a mouse
    * click or a mouse click with SHIFT key modifier.
    */
   class SelectRowHeadersOnMouseClicked extends MouseAdapter {
      @Override
      public void mousePressed(MouseEvent e) {
         setMouseClickingPoint(e);
         int startRowIndexAtSelection = tblRowNumberSheet.rowAtPoint(startMousePt);
         int endRowIndexAtSelection = tblRowNumberSheet.rowAtPoint(endMousePt);
         drawCellSelection(START_INDEX, startRowIndexAtSelection, END_INDEX, endRowIndexAtSelection);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         int[] selectedRows = tblBaseSheet.getSelectedRows();
         int startRowIndex = selectedRows[0];
         int endRowIndex = selectedRows[selectedRows.length - 1];
         setSelectionRange(START_INDEX, startRowIndex, END_INDEX, endRowIndex);
      }

      private void setMouseClickingPoint(MouseEvent e) {
         if (!isShiftKeyIsPressed(e)) {
            startMousePt = e.getPoint(); // set the initial clicking point, skip if SHIFT key is pressed
         }
         endMousePt = e.getPoint();
      }

      private boolean isShiftKeyIsPressed(MouseEvent e) {
         return e.isShiftDown();
      }
   }

   /**
    * Mouse adapter for selecting multiple row headers in the spreadsheet using
    * a mouse dragged gesture.
    */
   class SelectRowHeadersOnMouseDragged extends MouseAdapter {
      @Override
      public void mouseDragged(MouseEvent e) {
         int rowIndexAtInitialSelection = tblRowNumberSheet.rowAtPoint(startMousePt);
         int rowIndexAtCurrentSelection = tblRowNumberSheet.rowAtPoint(e.getPoint());
         drawCellSelection(START_INDEX, rowIndexAtInitialSelection, END_INDEX, rowIndexAtCurrentSelection);
      }
   }

   private void drawCellSelection(int startColumnIndex, int startRowIndex, int endColumnIndex, int endRowIndex) {
      tblBaseSheet.setColumnSelectionInterval(startColumnIndex,
            (endColumnIndex == END_INDEX ? tblBaseSheet.getColumnCount() - 1 : endColumnIndex));
      tblBaseSheet.setRowSelectionInterval(startRowIndex,
            (endRowIndex == END_INDEX ? tblBaseSheet.getRowCount() - 1 : endRowIndex));
   }
}
