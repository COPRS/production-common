package de.werum.coprs.ddip.frontend.service.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DdipExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	ResponseEntity<?> handleRuntimeException(final HttpServletRequest request, final Throwable e) {
		final RuntimeException ex = (RuntimeException) e;
		DdipRestController.LOGGER.error(ex.getMessage(), ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(DdipRestControllerException.class)
	@ResponseBody
	ResponseEntity<?> handleControllerException(final HttpServletRequest request, final Throwable e) {
		final DdipRestControllerException ex = (DdipRestControllerException) e;
		DdipRestController.LOGGER.error(ex.getMessage());

		return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
	}

}
