package de.werum.coprs.nativeapi.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NativeApiExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	ResponseEntity<?> handleRuntimeException(final HttpServletRequest request, final Throwable e) {
		final RuntimeException ex = (RuntimeException) e;
		NativeApiRestController.LOGGER.error(ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(NativeApiRestControllerException.class)
	@ResponseBody
	ResponseEntity<?> handleControllerException(final HttpServletRequest request, final Throwable e) {
		final NativeApiRestControllerException ex = (NativeApiRestControllerException) e;
		NativeApiRestController.LOGGER.error(ex.getMessage());

		return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
	}

}
