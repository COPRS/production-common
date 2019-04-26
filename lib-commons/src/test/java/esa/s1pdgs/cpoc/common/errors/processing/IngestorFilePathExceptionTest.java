package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorFilePathException;

/**
 * Test the exception FilePathException
 */
public class IngestorFilePathExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        IngestorFilePathException exception = new IngestorFilePathException(
                "path-test", "family", "msg exception");

        assertEquals(ErrorCode.INGESTOR_INVALID_PATH, exception.getCode());
        assertEquals("path-test", exception.getPath());
        assertEquals("family", exception.getFamily());
        assertTrue(exception.getMessage().contains("msg exception"));
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        IngestorFilePathException exception = new IngestorFilePathException(
                "path-test", "family", "msg exception");

        String log = exception.getLogMessage();
        assertTrue(log.contains("[path path-test]"));
        assertTrue(log.contains("[family family]"));
        assertTrue(log.contains("[msg "));
        assertTrue(log.contains("msg exception]"));
    }

}
