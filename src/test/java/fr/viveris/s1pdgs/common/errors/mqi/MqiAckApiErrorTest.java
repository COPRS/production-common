package fr.viveris.s1pdgs.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class MqiAckApiError
 * 
 * @author Viveris Technologies
 */
public class MqiAckApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {
        MqiAckApiError e1 = new MqiAckApiError(ProductCategory.EDRS_SESSIONS,
                123, "OK", "error-message");
        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals(123L, e1.getMessageId());
        assertEquals("OK", e1.getAckMessage());
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.MQI_ACK_API_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[messageId 123]"));
        assertTrue(str1.contains("[ackMessage OK]"));
        assertTrue(str1.contains("[msg error-message]"));

        MqiAckApiError e2 = new MqiAckApiError(ProductCategory.EDRS_SESSIONS,
                321, "ERROR: error", "error-message",
                new Exception("cause-message"));
        assertEquals(ProductCategory.EDRS_SESSIONS, e2.getCategory());
        assertEquals(321L, e2.getMessageId());
        assertEquals("ERROR: error", e2.getAckMessage());
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.MQI_ACK_API_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
