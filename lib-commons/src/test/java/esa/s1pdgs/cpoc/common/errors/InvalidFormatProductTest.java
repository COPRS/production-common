package esa.s1pdgs.cpoc.common.errors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * @author Viveris Technologies
 */
public class InvalidFormatProductTest {

    /**
     * Test the InternalErrorException
     */
    @Test
    public void testInternalErrorException() {
        InvalidFormatProduct e1 = new InvalidFormatProduct("erreur message");

        assertEquals(ErrorCode.INVALID_PRODUCT_FORMAT, e1.getCode());
        assertEquals("erreur message", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg erreur message]"));
    }

}
