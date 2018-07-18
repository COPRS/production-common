package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorIgnoredFileException;

/**
 * Test the exception IgnoredFileException
 */
public class IngestorIgnoredFileExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        IngestorIgnoredFileException exception =
                new IngestorIgnoredFileException("ignored-name");

        assertEquals(ErrorCode.INGESTOR_IGNORE_FILE, exception.getCode());
        assertTrue(exception.getMessage().contains("ignored-name"));
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        IngestorIgnoredFileException exception =
                new IngestorIgnoredFileException("ignored-name");

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg "));
        assertTrue(log.contains("ignored-name"));
    }

}
