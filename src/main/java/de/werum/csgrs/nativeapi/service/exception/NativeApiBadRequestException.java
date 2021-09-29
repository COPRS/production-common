package de.werum.csgrs.nativeapi.service.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class NativeApiBadRequestException extends NativeApiException {

	public NativeApiBadRequestException(final String message) {
		super(message, HttpStatus.BAD_REQUEST);
	}

	public NativeApiBadRequestException(final String message, final Throwable throwable) {
		super(message, throwable, HttpStatus.BAD_REQUEST);
	}

}
