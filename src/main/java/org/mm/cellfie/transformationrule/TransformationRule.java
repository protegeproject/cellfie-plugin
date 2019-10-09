package org.mm.cellfie.transformationrule;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.Nonnull;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

   @Expose @SerializedName("sheetName")
   private final String sheetName;
   
   @Expose @SerializedName("startColumn")
   private final String startColumn;
   
   @Expose @SerializedName("endColumn")
   private final String endColumn;
   
   @Expose @SerializedName("startRow")
   private final String startRow;
   
   @Expose @SerializedName("endRow")
   private final String endRow;
   
   @Expose @SerializedName("comment")
   private final String comment;
   
   @Expose @SerializedName("rule")
   private final String ruleExpression;

   public TransformationRule(@Nonnull String sheetName, @Nonnull String startColumn, @Nonnull String endColumn,
         @Nonnull String startRow, @Nonnull String endRow, @Nonnull String comment, @Nonnull String ruleExpression) {
      this.sheetName = checkNotNull(sheetName);
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
      return sheetName;
   }

   @Nonnull
   public String getStartColumn() { // 1-based index
      return startColumn;
   }

   @Nonnull
   public String getEndColumn() { // 1-based index
      return endColumn;
   }

   @Nonnull
   public String getStartRow() { // 1-based index
      return startRow;
   }

   @Nonnull
   public String getEndRow() { // 1-based index
      return endRow;
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
     return sheetName.equals(other.sheetName) && startColumn.equals(other.startColumn)
           && endColumn.equals(other.endColumn) && startRow.equals(other.startRow)
           && endRow.equals(other.endRow) && comment.equals(other.comment)
           && ruleExpression.equals(other.ruleExpression);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(sheetName, startColumn, endColumn, startRow, endRow, comment, ruleExpression);
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
            .add("sheetName", sheetName)
            .add("startColumn", startColumn)
            .add("endColumn", endColumn)
            .add("startRow", startRow)
            .add("endRow", endRow)
            .add("comment", comment)
            .add("ruleExpression", ruleExpression)
            .toString();
   }
}