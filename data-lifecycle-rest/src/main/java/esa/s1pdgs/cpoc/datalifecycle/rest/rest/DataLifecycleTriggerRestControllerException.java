package esa.s1pdgs.cpoc.datalifecycle.rest.rest;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
final class DataLifecycleTriggerRestControllerException extends RuntimeException
{
	private final HttpStatus status;
	
	public DataLifecycleTriggerRestControllerException(final String message, final HttpStatus status)
	{
		super(message);
		this.status = status;
	}
	
	public DataLifecycleTriggerRestControllerException(final String message, final Throwable throwable, final HttpStatus status)
	{
		super(message, throwable);
		this.status = status;
	}
	
	public HttpStatus getStatus()
	{
		return status;
	}
}