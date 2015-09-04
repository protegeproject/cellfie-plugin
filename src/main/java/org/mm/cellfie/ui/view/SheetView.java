package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.poi.ss.usermodel.Sheet;
import org.mm.cellfie.ui.model.SheetModel;

public class SheetView extends JPanel implements MMView
{
	private static final long serialVersionUID = 1L;

	private final Sheet sheet;
	private final SheetModel sheetModel;

	public SheetView(Sheet sheet)
	{
		this.sheet = sheet;
		sheetModel = new SheetModel(sheet);

		setLayout(new BorderLayout());

		SheetTable tblBaseSheet = new SheetTable(sheetModel);
		JScrollPane scrBaseSheet = new JScrollPane(tblBaseSheet);

		JTable tblRowNumberSheet = new RowNumberWrapper(tblBaseSheet);
		scrBaseSheet.setRowHeaderView(tblRowNumberSheet);
		scrBaseSheet.setCorner(JScrollPane.UPPER_LEFT_CORNER, tblRowNumberSheet.getTableHeader());

		add(BorderLayout.CENTER, scrBaseSheet);
		
		validate();
	}

	public String getSheetName()
	{
		return sheet.getSheetName();
	}

	@Override
	public void update()
	{
		// NO-OP
	}
}
