package esa.s1pdgs.cpoc.appcatalog.server.common.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;

public class RestExceptionHandlerTest {

    @Test
    public void testHandleIllegalArgumentException() {
        RestExceptionHandler handler = new RestExceptionHandler();

        ResponseEntity<String> error =
                handler.handle(new IllegalArgumentException("iae exception"));
        
        assertEquals(HttpStatus.PRECONDITION_FAILED, error.getStatusCode());
    }

    @Test
    public void testHandleRuntimeException() {
        RestExceptionHandler handler = new RestExceptionHandler();

        ResponseEntity<String> error =
                handler.handle(new RuntimeException("iae exception"));
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatusCode());
    }

    @Test
    public void testHandleRuntimeExceptionCustomHttpStatus() {
        RestExceptionHandler handler = new RestExceptionHandler();

        ResponseEntity<String> error =
                handler.handle(new RuntimeExceptionTestObj("iae exception"));
        
        assertEquals(HttpStatus.INSUFFICIENT_STORAGE, error.getStatusCode());
    }

    @Test
    public void testHandleAbstractAppDataException() {
        RestExceptionHandler handler = new RestExceptionHandler();

        ResponseEntity<String> error =
                handler.handle(new AppCatalogJobGenerationInvalidStateException("state", "DB"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertTrue(error.getBody().contains("Invalid job generation state"));

        error =
                handler.handle(new AppCatalogJobNotFoundException(125L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertTrue(error.getBody().contains("Job not found"));
    }
}
