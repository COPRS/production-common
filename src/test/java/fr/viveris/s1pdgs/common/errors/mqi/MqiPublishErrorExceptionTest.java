package fr.viveris.s1pdgs.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class MqiPublishApiError
 * 
 * @author Viveris Technologies
 */
public class MqiPublishErrorExceptionTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {

        MqiPublishErrorException e1 = new MqiPublishErrorException(
                "app-error-message", "error-message");
        assertEquals("app-error-message", e1.getErrorMessage());
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.MQI_PUBLISH_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[errorMessage app-error-message"));
        assertTrue(str1.contains("[msg error-message]"));

        MqiPublishErrorException e2 =
                new MqiPublishErrorException("app-error-message",
                        "error-message", new Exception("cause-message"));
        assertEquals("app-error-message", e1.getErrorMessage());
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.MQI_PUBLISH_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
