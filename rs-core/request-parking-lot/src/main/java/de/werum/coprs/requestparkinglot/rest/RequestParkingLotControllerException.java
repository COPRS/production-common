package de.werum.coprs.requestparkinglot.rest;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
final class RequestParkingLotControllerException extends RuntimeException
{
	private final HttpStatus status;
	
	public RequestParkingLotControllerException(final String message, final HttpStatus status)
	{
		super(message);
		this.status = status;
	}
	
	public RequestParkingLotControllerException(final String message, final Throwable throwable, final HttpStatus status)
	{
		super(message, throwable);
		this.status = status;
	}
	
	public HttpStatus getStatus()
	{
		return status;
	}
}