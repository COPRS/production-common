package de.werum.csgrs.nativeapi.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class NativeApiExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	ResponseEntity<?> handleRuntimeException(final HttpServletRequest request, final Throwable e) {
		final RuntimeException ex = (RuntimeException) e;
		NativeApiRestController.LOGGER.error(ex);

		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
