package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the exception MetadataCreationException
 */
public class MetadataNotPresentExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataNotPresentException exception = new MetadataNotPresentException("product-name");

        assertEquals(ErrorCode.ES_NOT_PRESENT_ERROR, exception.getCode());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataNotPresentException exception = new MetadataNotPresentException("product-name");

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg"));
    }


}
