package int_.esa.s1pdgs.cpoc.common.errors.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import int_.esa.s1pdgs.cpoc.common.ProductFamily;
import int_.esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import int_.esa.s1pdgs.cpoc.common.errors.obs.ObsException;

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
                new ObsException(ProductFamily.L0_PRODUCT, "key1",
                        new Throwable("throwable message"));

        assertEquals("key1", e1.getKey());
        assertEquals(ProductFamily.L0_PRODUCT, e1.getFamily());
        assertEquals(ErrorCode.OBS_ERROR, e1.getCode());
        assertEquals("throwable message", e1.getMessage());
        assertEquals("throwable message", e1.getCause().getMessage());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[key key1]"));
        assertTrue(str1.contains("[family L0_PRODUCT]"));
        assertTrue(str1.contains("[msg throwable message]"));
    }

}
