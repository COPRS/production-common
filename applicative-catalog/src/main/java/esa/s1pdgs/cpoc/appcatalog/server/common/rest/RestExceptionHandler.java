package esa.s1pdgs.cpoc.appcatalog.server.common.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AbstractAppDataException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * Handler REST exception
 * 
 * @author Viveris Technologies
 */
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(AbstractAppDataException.class)
    public ResponseEntity<String> handle(AbstractAppDataException ex) {

        // build HTTP information
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Class<?> exClass = ex.getClass();
        if (exClass.isAnnotationPresent(ResponseStatus.class)) {
            status = exClass.getAnnotation(ResponseStatus.class).value();
        }

        // Build error response
        String error = String.format(
                "{'status': %s, 'messsage': '%s', 'code': %s}", status.value(),
                ex.getLogMessage(), ex.getCode().getCode());

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException ex) {

        // build HTTP information
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Class<?> exClass = ex.getClass();
        if (exClass.isAnnotationPresent(ResponseStatus.class)) {
            status = exClass.getAnnotation(ResponseStatus.class).code();
        }

        // Build error response
        String error = String.format(
                "{'status': %s, 'messsage': '%s', 'code': %s}", status.value(),
                ex.getMessage(),
                AbstractCodedException.ErrorCode.INTERNAL_ERROR.getCode());

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handle(IllegalArgumentException ex) {

        // build HTTP information
        HttpStatus status = HttpStatus.PRECONDITION_FAILED;

        // Build error response
        String error = String.format(
                "{'status': %s, 'messsage': '%s', 'code': %s}", status.value(),
                ex.getMessage(),
                AbstractCodedException.ErrorCode.INTERNAL_ERROR.getCode());

        return new ResponseEntity<>(error, status);
    }

}
