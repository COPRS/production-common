package int_.esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import int_.esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import int_.esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;

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
