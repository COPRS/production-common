package de.werum.csgrs.nativeapi.rest;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class NativeApiRestControllerException extends RuntimeException {

	private final HttpStatus status;

	public NativeApiRestControllerException(final String message, final HttpStatus status) {
		super(message);
		this.status = status;
	}

	public NativeApiRestControllerException(final String message, final Throwable throwable, final HttpStatus status) {
		super(message, throwable);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
