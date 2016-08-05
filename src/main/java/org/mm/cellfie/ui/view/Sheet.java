package org.mm.cellfie.ui.view;

import org.apache.poi.hssf.util.CellReference;

public class Sheet
{
   private final String sheetName;
   private SelectionRange selectionRange = new SelectionRange(-1, -1, -1, -1);

   public Sheet(String sheetName)
   {
      this.sheetName = sheetName;
   }

   public String getName()
   {
      return sheetName;
   }

   public void setSelectionRange(int[] range)
   {
      selectionRange = new SelectionRange(range[0], range[1], range[2], range[3]);
   }

   public SelectionRange getSelectionRange()
   {
      return selectionRange;
   }

   class SelectionRange
   {
      private final int startColumn;
      private final int startRow;
      private final int endColumn;
      private final int endRow;

      public SelectionRange(int startColumn, int startRow, int endColumn, int endRow)
      {
         this.startColumn = startColumn;
         this.startRow = startRow;
         this.endColumn = endColumn;
         this.endRow = endRow;
      }

      public String getStartColumnName()
      {
         if (startColumn > -1) {
            return CellReference.convertNumToColString(startColumn);
         }
         return "";
      }

      public String getStartRowNumber()
      {
         if (startRow > -1) {
            return String.valueOf(startRow + 1); // 1-indexed
         }
         return "";
      }

      public String getEndColumnName()
      {
         if (endColumn > -1) {
            return CellReference.convertNumToColString(endColumn);
         }
         return "";
      }

      public String getEndRowNumber()
      {
         if (endRow > -1) {
            return String.valueOf(endRow + 1); // 1-indexed
         }
         return "";
      }

      public String getStartCell()
      {
         return getStartColumnName() + getStartRowNumber();
      }

      public String getEndCell()
      {
         return getEndColumnName() + getEndRowNumber();
      }

      @Override
      public String toString()
      {
         if (!getStartCell().isEmpty() && !getEndCell().isEmpty()) {
            return String.format("%s:%s", getStartCell(), getEndCell());
         }
         return "";
      }
   }
}
