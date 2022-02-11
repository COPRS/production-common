package esa.s1pdgs.cpoc.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class MqiStopApiError
 * 
 * @author Viveris Technologies
 */
public class MqiStopApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {
        MqiStopApiError e1 = new MqiStopApiError("error-message");
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.MQI_STOP_API_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg error-message]"));

        MqiStopApiError e2 = new MqiStopApiError("error-message",
                new Exception("cause-message"));
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.MQI_STOP_API_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
