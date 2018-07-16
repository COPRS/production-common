package fr.viveris.s1pdgs.common.errors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * @author Viveris Technologies
 */
public class AbstractCodedExceptionTest {

    @Test
    public void testEnumErroCode() {
        assertEquals(37, ErrorCode.values().length);

        assertEquals(ErrorCode.ES_CREATION_ERROR,
                ErrorCode.valueOf("ES_CREATION_ERROR"));
        assertEquals(ErrorCode.INTERNAL_ERROR,
                ErrorCode.valueOf("INTERNAL_ERROR"));
        assertEquals(ErrorCode.METADATA_FILE_EXTENSION,
                ErrorCode.valueOf("METADATA_FILE_EXTENSION"));
    }
}
