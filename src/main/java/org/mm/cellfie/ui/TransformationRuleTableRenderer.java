package org.mm.cellfie.ui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Represents the table renderer used to draw the transformation rule details in the
 * transformation rule browser. The renderer inherits text area to support multi-line
 * text.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
class TransformationRuleTableRenderer extends JTextArea implements TableCellRenderer {

   private static final long serialVersionUID = 1L;

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
         boolean hasFocus, int row, int column) {
      if (isSelected) {
         setForeground(table.getSelectionForeground());
         setBackground(table.getSelectionBackground());
      } else {
         setForeground(table.getForeground());
         setBackground(table.getBackground());
      }
      setFont(table.getFont());
      if (hasFocus) {
         setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
         if (table.isCellEditable(row, column)) {
            setForeground(UIManager.getColor("Table.focusCellForeground"));
            setBackground(UIManager.getColor("Table.focusCellBackground"));
         }
      } else {
         setBorder(new EmptyBorder(1, 2, 1, 2));
      }
      setText((value == null) ? "" : value.toString());
      return this;
   }
}
