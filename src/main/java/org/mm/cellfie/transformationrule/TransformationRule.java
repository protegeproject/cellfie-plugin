package org.mm.cellfie.transformationrule;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.Nonnull;
import org.mm.renderer.Sheet;
import org.mm.renderer.internal.CellUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Represents the data model for the MappingMaster transformation rule
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRule {

   public static final String START_COLUMN = "A";
   public static final String START_ROW = "1";
   public static final String ANY_WILDCARD = "+";

   private final Sheet sheet;
   private final String startColumn;
   private final String endColumn;
   private final String startRow;
   private final String endRow;
   private final String comment;
   private final String ruleExpression;

   public TransformationRule(@Nonnull Sheet sheet, @Nonnull String startColumn, @Nonnull String endColumn,
         @Nonnull String startRow, @Nonnull String endRow, @Nonnull String comment, @Nonnull String ruleExpression) {
      this.sheet = checkNotNull(sheet);
      this.startColumn = checkNotNull(startColumn);
      this.endColumn = checkNotNull(endColumn);
      this.startRow = checkNotNull(startRow);
      this.endRow = checkNotNull(endRow);
      this.comment = checkNotNull(comment);
      this.ruleExpression = checkNotNull(ruleExpression);
   }

   @Nonnull
   public String getRuleExpression() {
      return ruleExpression;
   }

   @Nonnull
   public String getComment() {
      return comment;
   }

   @Nonnull
   public String getSheetName() {
      return sheet.getSheetName();
   }

   @Nonnull
   public String getStartColumn() {
      return startColumn;
   }

   public int getStartColumnIndex() {
      return CellUtils.toColumnIndex(startColumn);
   }

   @Nonnull
   public String getEndColumn() {
      return endColumn;
   }

   public int getEndColumnIndex() {
      return (ANY_WILDCARD.equals(endColumn)) ? sheet.getEndColumnIndex() : CellUtils.toColumnIndex(endColumn);
   }

   @Nonnull
   public String getStartRow() {
      return startRow;
   }

   public int getStartRowIndex() {
      return CellUtils.toRowIndex(startRow);
   }

   @Nonnull
   public String getEndRow() {
      return endRow;
   }

   public int getEndRowIndex() {
      return (ANY_WILDCARD.equals(endRow)) ? sheet.getEndRowIndex() : CellUtils.toRowIndex(endRow);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
     }
     if (obj == this) {
         return true;
     }
     if (!(obj instanceof TransformationRule)) {
         return false;
     }
     TransformationRule other = (TransformationRule) obj;
     return getSheetName().equals(other.getSheetName()) && startColumn.equals(other.startColumn)
           && endColumn.equals(other.endColumn) && startRow.equals(other.startRow)
           && endRow.equals(other.endRow) && comment.equals(other.comment)
           && ruleExpression.equals(other.ruleExpression);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(getSheetName(), startColumn, endColumn, startRow, endRow, comment, ruleExpression);
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
            .add("sheetName", getSheetName())
            .add("startColumn", startColumn)
            .add("endColumn", endColumn)
            .add("startRow", startRow)
            .add("endRow", endRow)
            .add("comment", comment)
            .add("ruleExpression", ruleExpression)
            .toString();
   }
}