package esa.s1pdgs.cpoc.validation.rest;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class ValidationRestControllerException extends RuntimeException {

	private final HttpStatus status;

	public ValidationRestControllerException(final String message, final HttpStatus status) {
		super(message);
		this.status = status;
	}

	public ValidationRestControllerException(final String message, final Throwable throwable, final HttpStatus status) {
		super(message, throwable);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
