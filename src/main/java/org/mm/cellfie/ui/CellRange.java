package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import javax.annotation.Nonnull;
import org.apache.poi.ss.util.CellReference;

/**
 * Represents the cell range in a spreadsheet specified by the two coordinates:
 * the starting cell (C0,R0) and the ending cell (C1,R1).
 * <pre>
 *    (C0,R0)
 *       X------------------
 *       |                 |
 *       |    Selection    |
 *       |      Range      |
 *       |                 |
 *       ------------------X
 *                        (C1,R1)
 * </pre>
 * The range is defined as a 2-dimensional square that has a column length of C1-C0 and
 * a row length of R1-R0.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class CellRange {

   private final String sheetName;

   private final int startColumn;
   private final int startRow;
   private final int endColumn;
   private final int endRow;

   /**
    * Creates a cell range given the sheet name where the range selection is
    * happening, the start column index, the start row index, the end column
    * index and the end row index.
    * 
    * All the indexes start from zero, such that column index 0 is translated as
    * column A, and column index 3 is column D. Similarly, the row index 0 is
    * translated as row 1 and row index 3 is row 4.
    *
    * @param sheetName
    *           The name of the sheet where the cell range is originated
    * @param startColumn
    *           The starting column index. The index starts from 0 for column A
    * @param startRow
    *           The starting row index. The index starts from 0 for row 1
    * @param endColumn
    *           The ending column index. The index starts from 0 for column A
    * @param endRow
    *           The ending row index. The index starts from 0 for row 1
    */
   public CellRange(@Nonnull String sheetName, int startColumn, int startRow,
         int endColumn, int endRow) {
      this.sheetName = checkNotNull(sheetName);
      this.startColumn = startColumn; // C0
      this.startRow = startRow; // R0
      this.endColumn = endColumn; // C1
      this.endRow = endRow; // R1
   }

   public String getSheetName() {
      return sheetName;
   }

   public String getStartColumnName() {
      if (startColumn > -1) {
         return CellReference.convertNumToColString(startColumn);
      }
      return "";
   }

   public String getStartRowNumber() {
      if (startRow > -1) {
         return String.valueOf(startRow + 1); // 1-indexed
      }
      return "";
   }

   public String getEndColumnName() {
      if (endColumn > -1) {
         return CellReference.convertNumToColString(endColumn);
      }
      return "";
   }

   public String getEndRowNumber() {
      if (endRow > -1) {
         return String.valueOf(endRow + 1); // 1-indexed
      }
      return "";
   }

   public String getStartCell() {
      return getStartColumnName() + getStartRowNumber();
   }

   public String getEndCell() {
      return getEndColumnName() + getEndRowNumber();
   }

   public int getColumnRangeSize() {
      return endColumn - startColumn + 1;
   }

   public int getRowRangeSize() {
      return endRow - startRow + 1;
   }

   /**
    * Prints the cell range address denoted by the sheet name, start cell and end cell.
    * For example: <code>Sheet1!B2:H16</code>
    * 
    * @return Returns the range address string
    */
   public String getRangeAddress() {
      return format("%s!%s:%s", getSheetName(), getStartCell(), getEndCell());
   }

   @Override
   public String toString() {
      if (getStartCell().isEmpty()) {
         return "Unable to print the starting cell coordinate";
      }
      if (getEndCell().isEmpty()) {
         return "Unable to print the ending cell coordinate";
      }
      return getRangeAddress();
   }
}
