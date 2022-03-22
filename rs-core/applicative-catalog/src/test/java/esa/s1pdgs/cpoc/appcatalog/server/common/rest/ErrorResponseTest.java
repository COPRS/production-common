package esa.s1pdgs.cpoc.appcatalog.server.common.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ErrorResponseTest {

    @Test
    public void testGetters() {
        ErrorResponse error = new ErrorResponse(404, "error message", 1);
        assertEquals(404, error.getStatus());
        assertEquals("error message", error.getMessage());
        assertEquals(1, error.getCode());

        error.setMessage("other message");
        error.setStatus(4698);
        error.setCode(14);
        assertEquals(4698, error.getStatus());
        assertEquals("other message", error.getMessage());
        assertEquals(14, error.getCode());

    }
}
