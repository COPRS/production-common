package esa.s1pdgs.cpoc.common.errors.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObjectException;

/**
 * Test the class ObsUnknownObject
 * 
 * @author Viveris Technologies
 */
public class ObsUnknownObjectExceptionTest {

    /**
     * Test the ObsUnknownObjectException
     */
    @Test
    public void testObsUnknownObjectException() {
        ObsUnknownObjectException e1 =
                new ObsUnknownObjectException(ProductFamily.EDRS_SESSION, "key1");

        assertEquals("key1", e1.getKey());
        assertEquals(ProductFamily.EDRS_SESSION, e1.getFamily());
        assertEquals(ErrorCode.OBS_UNKOWN_OBJ, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[family EDRS_SESSION]"));
        assertTrue(str1.contains("[key key1]"));
    }

}
