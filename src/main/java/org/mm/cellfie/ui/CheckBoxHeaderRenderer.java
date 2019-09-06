package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Represents the renderer used to draw the select check-box at the table header.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class CheckBoxHeaderRenderer extends JCheckBox implements TableCellRenderer {

   private static final long serialVersionUID = 1L;

   public CheckBoxHeaderRenderer(@Nonnull JTable table) {
      super();
      checkNotNull(table);
      setHorizontalAlignment(JLabel.CENTER);
      setBorderPainted(true);
      setSelected(true);
      setOpaque(true);
      setMouseClickListener(table);
   }

   private void setMouseClickListener(JTable table) {
      final JTableHeader header = table.getTableHeader();
      header.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            if (isClickingRulePickColumnHeader(header, e.getPoint())) {
               toggleSelection();
               applySelection(table, isSelected());
            }
         }

         private boolean isClickingRulePickColumnHeader(JTableHeader header, Point clickPoint) {
            int columnIndex = header.columnAtPoint(clickPoint);
            return columnIndex == TransformationRuleTableModel.RULE_PICK_COLUMN;
         }

         private void toggleSelection() {
            setSelected(!isSelected());
         }

         private void applySelection(JTable table, boolean isSelected) {
            int rowSize = table.getRowCount();
            if (rowSize > 0) {
               for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                  table.setValueAt(isSelected, rowIndex, TransformationRuleTableModel.RULE_PICK_COLUMN);
               }
            }
         }
      });
   }

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
         boolean hasFocus, int row, int col) {
      TableCellRenderer defaultCellRenderer = table.getTableHeader().getDefaultRenderer();
      JLabel headerLabel = (JLabel) defaultCellRenderer.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, col);
      headerLabel.setIcon(new ComponentIcon(this));
      return headerLabel;
   }

   private class ComponentIcon implements Icon {

      private final JCheckBox check;

      public ComponentIcon(JCheckBox check) {
         this.check = check;
      }

      @Override
      public int getIconWidth() {
         return check.getPreferredSize().width;
      }

      @Override
      public int getIconHeight() {
         return check.getPreferredSize().height;
      }

      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
         SwingUtilities.paintComponent(g, check, (Container) c, x, y, getIconWidth(), getIconHeight());
      }
   }
}
