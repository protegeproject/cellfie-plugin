package org.mm.cellfie.ui.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class SheetTable extends JTable
{
   private static final long serialVersionUID = 1L;

   public SheetTable(TableModel model)
   {
      super(model);

      setDefaultRenderer(String.class, new WorksheetCellRenderer());

      JTableHeader header = new HighlightTableHeader(getColumnModel());
      header.setDefaultRenderer(new ColumnHeaderRenderer());
      setTableHeader(header);
      setGridColor(new Color(220, 220, 220));

      setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      setColumnSelectionAllowed(true);
      setRowSelectionAllowed (false);
//      setCellSelectionEnabled(true);
      resizeColumnWidth();
      setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
   }

   private void resizeColumnWidth()
   {
      final TableColumnModel columnModel = getColumnModel();
      for (int column = 0; column < getColumnCount(); column++) {
         int width = 50; // Min width
         for (int row = 0; row < getRowCount(); row++) {
            TableCellRenderer renderer = getCellRenderer(row, column);
            Component comp = prepareRenderer(renderer, row, column);
            width = Math.max(comp.getPreferredSize().width + 1, width);
         }
         columnModel.getColumn(column).setPreferredWidth(width);
      }
   }

   public static class HighlightTableHeader extends JTableHeader
   {
      private static final long serialVersionUID = 1L;

      public HighlightTableHeader(TableColumnModel model)
      {
         super(model);
      }

      @Override
      public void columnSelectionChanged(ListSelectionEvent e)
      {
         repaint();
      }
   }

   /*
    * Custom cell renderer for the table header
    */
   public class ColumnHeaderRenderer extends DefaultTableCellRenderer
   {
      private static final long serialVersionUID = 1L;

      public ColumnHeaderRenderer()
      {
         setHorizontalAlignment(JLabel.CENTER);
      }

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused,
            int row, int column)
      {
         super.getTableCellRendererComponent(table, value, selected, focused, row, column);
         if (table.isColumnSelected(column)) {
            setForeground(UIManager.getColor("Table.selectionForeground"));
            setBackground(UIManager.getColor("controlShadow"));
         } else {
            setForeground(UIManager.getColor("TableHeader.foreground"));
            setBackground(UIManager.getColor("Table.background"));
         }
         setBorder(UIManager.getBorder("TableHeader.cellBorder"));
         return this;
      }
   }

   /*
    * Custom cell renderer for the table sheet
    */
   public class WorksheetCellRenderer extends DefaultTableCellRenderer
   {
      private static final long serialVersionUID = 1L;

      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            final int row, int column)
      {
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
            cell.setBorder(new CompoundBorder(new EmptyBorder(new Insets(1, 4, 1, 4)), cell.getBorder()));
         }

         this.setOpaque(true);
         setText((String) value);
         return this;
      }
   }
}
