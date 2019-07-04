package esa.s1pdgs.cpoc.reqrepo.rest;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
final class RequestRepositoryControllerException extends RuntimeException
{
	private final HttpStatus status;
	
	public RequestRepositoryControllerException(final String message, final HttpStatus status)
	{
		super(message);
		this.status = status;
	}
	
	public RequestRepositoryControllerException(final String message, final Throwable throwable, final HttpStatus status)
	{
		super(message, throwable);
		this.status = status;
	}
	
	public HttpStatus getStatus()
	{
		return status;
	}
}