package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;
import org.mm.renderer.Sheet;
import org.mm.renderer.internal.CellUtils;

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
      return sheet.getEndRowNumber();
   }

   @Override
   public int getColumnCount() {
      return sheet.getEndColumnNumber();
   }

   @Override
   public String getColumnName(int column) {
      return CellUtils.toColumnLabel(column+1);
   }

   @Override
   public Object getValueAt(int row, int column) {
      Optional<String> value = sheet.getValueFromCell(column+1, row+1);
      return (value.isPresent()) ? value.get() : "";
   }
}