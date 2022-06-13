package esa.s1pdgs.cpoc.common.errors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InvalidFormatProductException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * @author Viveris Technologies
 */
public class InvalidFormatProductExceptionTest {

    /**
     * Test the InternalErrorException
     */
    @Test
    public void testInternalErrorException() {
        InvalidFormatProductException e1 = new InvalidFormatProductException("erreur message");

        assertEquals(ErrorCode.INVALID_PRODUCT_FORMAT, e1.getCode());
        assertEquals("erreur message", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg erreur message]"));
    }

}
