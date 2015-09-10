package org.mm.cellfie.ui.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
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

		JTableHeader header = new HighlightTableHeader(getColumnModel());
		header.setDefaultRenderer(new ColumnHeaderRenderer());
		setTableHeader(header);

		resizeColumnWidth();
		setCellSelectionEnabled(true);
		setIntercellSpacing(new Dimension(4,4));
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setGridColor(new Color(220, 220, 220));
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

	public static class ColumnHeaderRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public ColumnHeaderRenderer()
		{
			setHorizontalAlignment(JLabel.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column)
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
}
