package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIllegalFileExtensionException;

/**
 * Test the exception FilePathException
 */
public class MetadataIllegalFileExtensionExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataIllegalFileExtensionException exception =
                new MetadataIllegalFileExtensionException("exten");

        assertEquals(ErrorCode.METADATA_FILE_EXTENSION, exception.getCode());
        assertEquals(ErrorCode.METADATA_FILE_EXTENSION.getCode(),
                exception.getCode().getCode());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataIllegalFileExtensionException exception =
                new MetadataIllegalFileExtensionException("exten");

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg "));
    }

}
