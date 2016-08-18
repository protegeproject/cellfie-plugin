package org.mm.cellfie.ui.view;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/*
 *	Use a JTable as a renderer for row numbers of a given main table.
 *  This table must be added to the row header of the scroll pane that
 *  contains the main table.
 *
 *  Original source code: http://www.camick.com/java/source/RowNumberTable.java
 */
public class RowNumberWrapper extends JTable implements ChangeListener, PropertyChangeListener, TableModelListener
{
   private static final long serialVersionUID = 1L;

   private JTable main;

   public RowNumberWrapper(JTable table)
   {
      main = table;
      main.addPropertyChangeListener(this);
      main.getModel().addTableModelListener(this);

      setFocusable(false);
      setAutoCreateColumnsFromModel(false);
      setSelectionModel(main.getSelectionModel());
//      setCellSelectionEnabled(true);
      setColumnSelectionAllowed(false);
      setRowSelectionAllowed(true);

      TableColumn column = new TableColumn();
      column.setHeaderValue(" ");
      addColumn(column);
      column.setCellRenderer(new RowNumberRenderer());

      getColumnModel().getColumn(0).setPreferredWidth(50);
      setPreferredScrollableViewportSize(getPreferredSize());
   }

   @Override
   public void addNotify()
   {
      super.addNotify();

      Component c = getParent();

      // Keep scrolling of the row table in sync with the main table.

      if (c instanceof JViewport) {
         JViewport viewport = (JViewport) c;
         viewport.addChangeListener(this);
      }
   }

   /*
    * Delegate method to main table
    */
   @Override
   public int getRowCount()
   {
      return main.getRowCount();
   }

   @Override
   public int getRowHeight(int row)
   {
      int rowHeight = main.getRowHeight(row);

      if (rowHeight != super.getRowHeight(row)) {
         super.setRowHeight(row, rowHeight);
      }

      return rowHeight;
   }

   /*
    * No model is being used for this table so just use the row number as the
    * value of the cell.
    */
   @Override
   public Object getValueAt(int row, int column)
   {
      return Integer.toString(row + 1);
   }

   /*
    * Don't edit data in the main TableModel by mistake
    */
   @Override
   public boolean isCellEditable(int row, int column)
   {
      return false;
   }

   /*
    * Do nothing since the table ignores the model
    */
   @Override
   public void setValueAt(Object value, int row, int column)
   {
      // NO-OP
   }

   @Override
   public void stateChanged(ChangeEvent e)
   {
      // Keep the scrolling of the row table in sync with main table

      JViewport viewport = (JViewport) e.getSource();
      JScrollPane scrollPane = (JScrollPane) viewport.getParent();
      scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
   }

   public void propertyChange(PropertyChangeEvent e)
   {
      // Keep the row table in sync with the main table

      if ("selectionModel".equals(e.getPropertyName())) {
         setSelectionModel(main.getSelectionModel());
      }

      if ("rowHeight".equals(e.getPropertyName())) {
         repaint();
      }

      if ("model".equals(e.getPropertyName())) {
         main.getModel().addTableModelListener(this);
         revalidate();
      }
   }

   @Override
   public void tableChanged(TableModelEvent e)
   {
      revalidate();
   }

   /*
    * Attempt to mimic the table header renderer
    */
   private static class RowNumberRenderer extends DefaultTableCellRenderer
   {
      private static final long serialVersionUID = 1L;

      public RowNumberRenderer()
      {
         setHorizontalAlignment(JLabel.CENTER);
      }

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
      {
         if (table.isRowSelected(row)) {
            setForeground(UIManager.getColor("Table.selectionForeground"));
            setBackground(UIManager.getColor("controlShadow"));
         } else {
            setForeground(UIManager.getColor("TableHeader.foreground"));
            setBackground(UIManager.getColor("Table.background"));
         }
         setText((value == null) ? "" : value.toString());
         setBorder(UIManager.getBorder("TableHeader.cellBorder"));

         return this;
      }
   }
}