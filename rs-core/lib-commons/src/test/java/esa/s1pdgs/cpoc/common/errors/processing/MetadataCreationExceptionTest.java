package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;


/**
 * Test the exception MetadataCreationException
 */

public class MetadataCreationExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataCreationException exception = new MetadataCreationException("product-name",
                "test-result", "test-status");
        
        assertEquals(ErrorCode.ES_CREATION_ERROR, exception.getCode());
        assertEquals("test-result", exception.getResult());
        assertEquals("test-status", exception.getStatus());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataCreationException exception = new MetadataCreationException("product-name",
                "test-result", "test-status");
        
        String log = exception.getLogMessage();
        assertTrue(log.contains("[result test-result]"));
        assertTrue(log.contains("[status test-status]"));
        assertTrue(log.contains("[msg"));
    }


}
