package fr.viveris.s1pdgs.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the exception MetadataCreationException
 */
public class MetadataExtractionExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataExtractionException exception =
                new MetadataExtractionException(new Exception("message error"));

        assertEquals(ErrorCode.METADATA_EXTRACTION_ERROR, exception.getCode());
        assertEquals("message error", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataExtractionException exception =
                new MetadataExtractionException(new Exception("message error"));

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg message error]"));
    }

}
