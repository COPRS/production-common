package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;

/**
 * Test the exception MetadataCreationException
 */
public class MetadataMalformedExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataMalformedException exception =
                new MetadataMalformedException("test-result");

        assertEquals(ErrorCode.METADATA_MALFORMED_ERROR, exception.getCode());
        assertEquals("test-result", exception.getMissingField());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataMalformedException exception =
                new MetadataMalformedException("test-result");

        String log = exception.getLogMessage();
        assertTrue(log.contains("[missingField test-result]"));
        assertTrue(log.contains("[msg"));
    }

}
