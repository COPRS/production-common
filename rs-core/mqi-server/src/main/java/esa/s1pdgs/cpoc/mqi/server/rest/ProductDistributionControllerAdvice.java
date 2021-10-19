package esa.s1pdgs.cpoc.mqi.server.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import esa.s1pdgs.cpoc.mqi.server.rest.ProductDistributionController.ProductDistributionException;

@ControllerAdvice(basePackageClasses = ProductDistributionController.class)
public class ProductDistributionControllerAdvice extends ResponseEntityExceptionHandler {
	
    @ExceptionHandler(ProductDistributionException.class)
    @ResponseBody
	ResponseEntity<?> handleControllerException(final HttpServletRequest request, final Throwable e) {
		ProductDistributionController.LOGGER.error("could not handle request {}", request.getRequestURI(), e);
		return new ResponseEntity<Void>(((ProductDistributionException) e).getStatus());
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	public ResponseEntity<?> handle(final HttpServletRequest request, RuntimeException e) {
		ProductDistributionController.LOGGER.error("could not handle request {}", request.getRequestURI(), e);
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
