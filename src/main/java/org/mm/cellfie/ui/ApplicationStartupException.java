package org.mm.cellfie.ui;

import org.mm.exceptions.MappingMasterException;

public class ApplicationStartupException extends MappingMasterException
{
	private static final long serialVersionUID = 1L;

	public ApplicationStartupException(String message)
	{
		super(message);
	}

	public ApplicationStartupException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
