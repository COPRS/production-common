package fr.viveris.s1pdgs.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

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
