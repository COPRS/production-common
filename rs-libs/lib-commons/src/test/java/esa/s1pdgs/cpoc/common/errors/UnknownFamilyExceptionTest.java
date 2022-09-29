package esa.s1pdgs.cpoc.common.errors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class UnknownFamilyException
 * 
 * @author Viveris Technologies
 */
public class UnknownFamilyExceptionTest {

    /**
     * Test the ObsUnknownObjectException
     */
    @Test
    public void testObsUnknownFamilyException() {
        UnknownFamilyException e1 =
                new UnknownFamilyException("inv-family", "error message");

        assertEquals("inv-family", e1.getFamily());
        assertEquals(ErrorCode.UNKNOWN_FAMILY, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg error message]"));
        assertTrue(str1.contains("[family inv-family]"));
    }

}
