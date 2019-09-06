package org.mm.cellfie.ui;

import java.awt.Component;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Represents the renderer used by the {@code SheetTable} to draw each cell in
 * the target sheet.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class SheetCellRenderer extends DefaultTableCellRenderer {

   private static final long serialVersionUID = 1L;

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
         boolean hasFocus, final int row, int column) {
      JLabel cell = (JLabel) (this);
      if (hasFocus) {
         setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
         cell = null;
      } else {
         setBackground(table.getBackground());
         setBorder(null);
      }
      if (isSelected) {
         setBackground(table.getSelectionBackground());
         setBorder(null);
      } else {
         setBackground(table.getBackground());
         setBorder(null);
      }
      if (cell != null) {
         final Border cellBorder = cell.getBorder();
         cell.setBorder(new CompoundBorder(new EmptyBorder(new Insets(1, 4, 1, 4)), cellBorder));
      }
      this.setOpaque(true);
      setText((String) value);
      return this;
   }
} 
