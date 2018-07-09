package fr.viveris.s1pdgs.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the exception FilePathException
 */
public class MetadataIllegalFileExtensionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataIllegalFileExtension exception =
                new MetadataIllegalFileExtension("exten");

        assertEquals(ErrorCode.METADATA_FILE_EXTENSION, exception.getCode());
        assertEquals(ErrorCode.METADATA_FILE_EXTENSION.getCode(),
                exception.getCode().getCode());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataIllegalFileExtension exception =
                new MetadataIllegalFileExtension("exten");

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg "));
    }

}
