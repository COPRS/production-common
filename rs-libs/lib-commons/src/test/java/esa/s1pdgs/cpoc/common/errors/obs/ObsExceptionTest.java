package esa.s1pdgs.cpoc.common.errors.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;

/**
 * Test the class ObsException
 * 
 * @author Viveris Technologies
 */
public class ObsExceptionTest {

    /**
     * Test the ObsS3Exception
     */
    @Test
    public void testObsS3Exception() {
        ObsException e1 =
                new ObsException(ProductFamily.L0_SLICE, "key1",
                        new Throwable("throwable message"));

        assertEquals("key1", e1.getKey());
        assertEquals(ProductFamily.L0_SLICE, e1.getFamily());
        assertEquals(ErrorCode.OBS_ERROR, e1.getCode());
        assertEquals("throwable message", e1.getMessage());
        assertEquals("throwable message", e1.getCause().getMessage());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[key key1]"));
        assertTrue(str1.contains("[family L0_SLICE]"));
        assertTrue(str1.contains("[msg throwable message]"));
    }

}
