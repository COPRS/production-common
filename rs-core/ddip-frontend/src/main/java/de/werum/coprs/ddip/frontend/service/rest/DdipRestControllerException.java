package de.werum.coprs.ddip.frontend.service.rest;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class DdipRestControllerException extends RuntimeException {

	private final HttpStatus status;

	public DdipRestControllerException(final String message, final HttpStatus status) {
		super(message);
		this.status = status;
	}

	public DdipRestControllerException(final String message, final Throwable throwable, final HttpStatus status) {
		super(message, throwable);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
