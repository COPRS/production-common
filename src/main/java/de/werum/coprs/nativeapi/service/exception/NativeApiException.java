package de.werum.coprs.nativeapi.service.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class NativeApiException extends RuntimeException {

	protected final HttpStatus status;

	public NativeApiException(final String message, final HttpStatus status) {
		super(message);
		this.status = status;
	}

	public NativeApiException(final String message, final Throwable throwable, final HttpStatus status) {
		super(message, throwable);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return this.status;
	}
}
