package esa.s1pdgs.cpoc.common.errors.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.obs.ObsAlreadyExist;

/**
 * Test the exception ObsAlreadyExist
 */
public class ObsAlreadyExistTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        Throwable cause = new Exception("cause exception");
        ObsAlreadyExist exception = new ObsAlreadyExist(
                ProductFamily.AUXILIARY_FILE, "key-test", cause);

        assertEquals(ErrorCode.OBS_ALREADY_EXIST, exception.getCode());
        assertEquals("key-test", exception.getKey());
        assertEquals(ProductFamily.AUXILIARY_FILE, exception.getFamily());
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("cause exception"));
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        Throwable cause = new Exception("cause exception");
        ObsAlreadyExist exception = new ObsAlreadyExist(
                ProductFamily.AUXILIARY_FILE, "key-test", cause);

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg "));
        assertTrue(log.contains("cause exception]"));
    }
}
