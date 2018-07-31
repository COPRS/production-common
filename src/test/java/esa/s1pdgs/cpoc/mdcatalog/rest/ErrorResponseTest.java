package esa.s1pdgs.cpoc.mdcatalog.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ErrorResponseTest {

    @Test
    public void testGetters() {
        ErrorResponse error = new ErrorResponse();
        error.setMessage("error message");
        error.setStatus(4698);
        assertEquals("error message", error.getMessage());
        assertEquals(4698, error.getStatus());
    }
}
