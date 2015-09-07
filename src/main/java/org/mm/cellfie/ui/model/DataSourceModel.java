package org.mm.cellfie.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.mm.ss.SpreadSheetDataSource;
import org.mm.ui.MMModel;

public class DataSourceModel implements MMModel
{
	private SpreadSheetDataSource dataSource;

	public DataSourceModel()
	{
		this(null);
	}

	public DataSourceModel(SpreadSheetDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean isEmpty()
	{
		return dataSource == null;
	}

	public SpreadSheetDataSource getDataSource()
	{
		return dataSource;
	}

	public List<String> getSheetNames()
	{
		List<String> sheetNames = new ArrayList<String>();
		if (dataSource != null) {
			sheetNames.addAll(dataSource.getSheetNames());
		}
		return sheetNames;
	}
}
