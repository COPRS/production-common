package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIgnoredFileException;

/**
 * Test the exception IgnoredFileException
 */
public class MetadataIgnoredFileExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataIgnoredFileException exception =
                new MetadataIgnoredFileException("ignored-name");

        assertEquals(ErrorCode.METADATA_IGNORE_FILE, exception.getCode());
        assertTrue(exception.getMessage().contains("ignored-name"));
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataIgnoredFileException exception =
                new MetadataIgnoredFileException("ignored-name");

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg "));
        assertTrue(log.contains("ignored-name"));
    }

}
