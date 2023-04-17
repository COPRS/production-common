package de.werum.coprs.nativeapi.rest;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class StacRestControllerException extends RuntimeException {

	private final HttpStatus status;

	public StacRestControllerException(final String message, final HttpStatus status) {
		super(message);
		this.status = status;
	}

	public StacRestControllerException(final String message, final Throwable throwable, final HttpStatus status) {
		super(message, throwable);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
