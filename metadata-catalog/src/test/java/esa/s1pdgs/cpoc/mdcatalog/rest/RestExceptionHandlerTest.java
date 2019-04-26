package esa.s1pdgs.cpoc.mdcatalog.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RestExceptionHandlerTest {

    @Test
    public void testHandleIllegalArgumentException() {
        RestExceptionHandler handler = new RestExceptionHandler();

        ResponseEntity<ErrorResponse> error =
                handler.handle(new IllegalArgumentException("iae exception"));
        
        assertEquals("iae exception", error.getBody().getMessage());
        assertEquals(HttpStatus.PRECONDITION_FAILED, error.getStatusCode());
    }

    @Test
    public void testHandleRuntimeException() {
        RestExceptionHandler handler = new RestExceptionHandler();

        ResponseEntity<ErrorResponse> error =
                handler.handle(new RuntimeException("iae exception"));
        
        assertEquals("iae exception", error.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatusCode());
    }

    @Test
    public void testHandleRuntimeExceptionCustomHttpStatus() {
        RestExceptionHandler handler = new RestExceptionHandler();

        ResponseEntity<ErrorResponse> error =
                handler.handle(new RuntimeExceptionTestObj("iae exception"));
        
        assertEquals("iae exception", error.getBody().getMessage());
        assertEquals(HttpStatus.INSUFFICIENT_STORAGE, error.getStatusCode());
    }
}
