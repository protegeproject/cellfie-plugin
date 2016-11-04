package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import org.mm.workbook.Sheet;

/**
 * Represents the sheet panel used to display the cells (or the data) of a
 * spreadsheet in a table structure.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
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
    * Returns the name of the sheet presented by the sheet panel.
    *
    * @return The sheet name.
    */
   public String getSheetName() {
      return sheet.getName();
   }

   private void setSelectedCellRange(int startColumnIndex, int startRowIndex, int endColumnIndex,
         int endRowIndex) {
      this.startColumnIndex = startColumnIndex;
      this.startRowIndex = startRowIndex;
      this.endColumnIndex = endColumnIndex;
      this.endRowIndex = endRowIndex;
   }

   /**
    * Returns the selection range captured by the sheet panel.
    *
    * @return The selection range.
    */
   public CellRange getSelectedCellRange() {
      return new CellRange(getSheetName(),
            startColumnIndex,
            startRowIndex,
            endColumnIndex,
            endRowIndex);
   }

   /**
    * Mouse adapter for selecting a cell range in the spreadsheet
    */
   private class SelectCellRange extends MouseAdapter {
      @Override
      public void mouseReleased(MouseEvent e) {
         int[] selectedColumns = tblBaseSheet.getSelectedColumns();
         int[] selectedRows = tblBaseSheet.getSelectedRows();
         if (selectedColumns.length == 0 || selectedRows.length == 0) {
            setSelectedCellRange(START_INDEX, START_INDEX, START_INDEX, END_INDEX); // A:A
         } else {
            setSelectedCellRange(selectedColumns[0], selectedRows[0],
                  selectedColumns[selectedColumns.length - 1],
                  selectedRows[selectedRows.length - 1]);
         }
      }
   }

   /**
    * Mouse adapter for selecting the column header in the spreadsheet using a
    * mouse click or a mouse click with SHIFT key modifier.
    */
   private class SelectColumnHeadersOnMouseClicked extends MouseAdapter {
      @Override
      public void mousePressed(MouseEvent e) {
         setMouseClickingPoint(e);
         int startColumnIndexAtSelection = header.columnAtPoint(startMousePt);
         int endColumnIndexAtSelection = header.columnAtPoint(endMousePt);
         drawCellSelection(
               startColumnIndexAtSelection,
               START_INDEX,
               endColumnIndexAtSelection,
               END_INDEX);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         int[] selectedColumns = tblBaseSheet.getSelectedColumns();
         int startColumnIndex = selectedColumns[0];
         int endColumnIndex = selectedColumns[selectedColumns.length - 1];
         setSelectedCellRange(startColumnIndex, START_INDEX, endColumnIndex, END_INDEX);
      }

      private void setMouseClickingPoint(MouseEvent e) {
         // set the initial clicking point, but ignore if SHIFT key is pressed
         if (!isShiftKeyIsPressed(e)) {
            startMousePt = e.getPoint();
         }
         endMousePt = e.getPoint();
      }

      private boolean isShiftKeyIsPressed(MouseEvent e) {
         return e.isShiftDown();
      }
   }

   /**
    * Mouse adapter for selecting multiple column headers in the spreadsheet
    * using a mouse dragged gesture.
    */
   private class SelectColumnHeadersOnMouseDragged extends MouseAdapter {
      @Override
      public void mouseDragged(MouseEvent e) {
         int columnIndexAtInitialSelection = header.columnAtPoint(startMousePt);
         int columnIndexAtCurrentSelection = header.columnAtPoint(e.getPoint());
         drawCellSelection(
               columnIndexAtInitialSelection,
               START_INDEX,
               columnIndexAtCurrentSelection,
               END_INDEX);
      }
   }

   /**
    * Mouse adapter for selecting the row header in the spreadsheet using a
    * mouse click or a mouse click with SHIFT key modifier.
    */
   private class SelectRowHeadersOnMouseClicked extends MouseAdapter {
      @Override
      public void mousePressed(MouseEvent e) {
         setMouseClickingPoint(e);
         int startRowIndexAtSelection = tblRowNumberSheet.rowAtPoint(startMousePt);
         int endRowIndexAtSelection = tblRowNumberSheet.rowAtPoint(endMousePt);
         drawCellSelection(
               START_INDEX,
               startRowIndexAtSelection,
               END_INDEX,
               endRowIndexAtSelection);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         int[] selectedRows = tblBaseSheet.getSelectedRows();
         int startRowIndex = selectedRows[0];
         int endRowIndex = selectedRows[selectedRows.length - 1];
         setSelectedCellRange(START_INDEX, startRowIndex, END_INDEX, endRowIndex);
      }

      private void setMouseClickingPoint(MouseEvent e) {
         // set the initial clicking point, but ignore if SHIFT key is pressed
         if (!isShiftKeyIsPressed(e)) {
            startMousePt = e.getPoint();
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
   private class SelectRowHeadersOnMouseDragged extends MouseAdapter {
      @Override
      public void mouseDragged(MouseEvent e) {
         int rowIndexAtInitialSelection = tblRowNumberSheet.rowAtPoint(startMousePt);
         int rowIndexAtCurrentSelection = tblRowNumberSheet.rowAtPoint(e.getPoint());
         drawCellSelection(
               START_INDEX,
               rowIndexAtInitialSelection,
               END_INDEX,
               rowIndexAtCurrentSelection);
      }
   }

   private void drawCellSelection(int startColumnIndex, int startRowIndex, int endColumnIndex,
         int endRowIndex) {
      tblBaseSheet.setColumnSelectionInterval(startColumnIndex,
            (endColumnIndex == END_INDEX ? tblBaseSheet.getColumnCount() - 1 : endColumnIndex));
      tblBaseSheet.setRowSelectionInterval(startRowIndex,
            (endRowIndex == END_INDEX ? tblBaseSheet.getRowCount() - 1 : endRowIndex));
   }
}
