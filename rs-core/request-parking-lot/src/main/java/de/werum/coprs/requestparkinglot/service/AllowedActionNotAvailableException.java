package de.werum.coprs.requestparkinglot.service;

@SuppressWarnings("serial")
public class AllowedActionNotAvailableException extends Exception {
	
	public AllowedActionNotAvailableException(final String message)
	{
		super(message);
	}

}
