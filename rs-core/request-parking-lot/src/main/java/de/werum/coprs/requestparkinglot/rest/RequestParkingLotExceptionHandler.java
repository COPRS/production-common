package de.werum.coprs.requestparkinglot.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RequestParkingLotExceptionHandler {	
	@ExceptionHandler(RequestParkingLotControllerException.class)
	@ResponseBody
	ResponseEntity<?> handleControllerException(final HttpServletRequest _request, final Throwable _e) {
		final RequestParkingLotControllerException ex = (RequestParkingLotControllerException) _e;
		RequestParkingLotController.LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(ex.getStatus());
	}
	
	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	ResponseEntity<?> handleRuntimeException(final HttpServletRequest _request, final Throwable _e) {
		final RuntimeException ex = (RuntimeException) _e;
		RequestParkingLotController.LOGGER.error(ex);
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(OutOfMemoryError.class)
	@ResponseBody
	ResponseEntity<?> handleOOMError(final HttpServletRequest _request, final Throwable _e) {
		final OutOfMemoryError ex = (OutOfMemoryError) _e;
		RequestParkingLotController.LOGGER.error(ex);
		return new ResponseEntity<>("Could not retrieve data as retrieved data exceeded memory.", HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
