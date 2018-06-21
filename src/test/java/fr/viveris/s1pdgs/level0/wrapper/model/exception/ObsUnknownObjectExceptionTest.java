package fr.viveris.s1pdgs.level0.wrapper.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException.ErrorCode;

/**
 * Test the class ObsUnknownObjectException
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
                new ObsUnknownObjectException(ProductFamily.RAW, "key1");

        assertEquals("key1", e1.getKey());
        assertEquals(ProductFamily.RAW, e1.getFamily());
        assertEquals(ErrorCode.OBS_UNKOWN_OBJ, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[family RAW]"));
        assertTrue(str1.contains("[key key1]"));
    }

}
