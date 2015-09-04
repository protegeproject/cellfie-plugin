package org.mm.cellfie.ui.model;

import javax.swing.table.AbstractTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.mm.ss.SpreadSheetUtil;

public class SheetModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	private final Sheet sheet;

	public SheetModel(Sheet sheet)
	{
		this.sheet = sheet;
	}

	public int getRowCount()
	{
		return sheet.getLastRowNum() + 1;
	}

	public int getColumnCount()
	{
		int maxCount = 0;
		for (int i = 0; i < getRowCount(); i++) {
			int currentCount = sheet.getRow(i).getLastCellNum();
			if (currentCount > maxCount) {
				maxCount = currentCount;
			}
		}
		return maxCount;
	}

	public String getColumnName(int column)
	{
		return SpreadSheetUtil.columnNumber2Name(column+1);
	}

	public Object getValueAt(int row, int column)
	{
		try {
			Cell cell = sheet.getRow(row).getCell(column);
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK: return "";
				case Cell.CELL_TYPE_STRING: return cell.getStringCellValue();
				case Cell.CELL_TYPE_NUMERIC: 
					if (isInteger(cell.getNumericCellValue())) { // check if the numeric is an integer or double
						return (int) cell.getNumericCellValue();
					} else {
						return cell.getNumericCellValue();
					}
				case Cell.CELL_TYPE_BOOLEAN: return cell.getBooleanCellValue();
				case Cell.CELL_TYPE_FORMULA: return cell.getNumericCellValue();
				default: return "";
			}
		} catch (NullPointerException e) {
			// TODO Log this strange error
			return "";
		}
	}

	private boolean isInteger(double number)
	{
		return (number == Math.floor(number) && !Double.isInfinite(number));
	}
}
