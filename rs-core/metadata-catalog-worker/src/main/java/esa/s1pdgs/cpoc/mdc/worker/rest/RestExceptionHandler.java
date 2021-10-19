package esa.s1pdgs.cpoc.mdc.worker.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handle(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(ex.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Class<?> exClass = ex.getClass();
        if (exClass.isAnnotationPresent(ResponseStatus.class)) {
            status = exClass.getAnnotation(ResponseStatus.class).code();
        }
        error.setStatus(status.value());

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handle(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.PRECONDITION_FAILED);
    }

}
