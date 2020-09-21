package esa.s1pdgs.cpoc.odip.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import esa.s1pdgs.cpoc.odip.service.OnDemandService;

@ControllerAdvice
public class OnDemandRestControllerAdvice {
	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	ResponseEntity<?> handleRuntimeException(final HttpServletRequest _request, final Throwable _e) {
		final RuntimeException ex = (RuntimeException) _e;
		OnDemandService.LOGGER.error(ex);
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
