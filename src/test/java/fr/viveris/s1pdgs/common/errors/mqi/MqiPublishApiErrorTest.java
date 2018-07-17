package fr.viveris.s1pdgs.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.ResumeDetails;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class MqiPublishApiError
 * 
 * @author Viveris Technologies
 */
public class MqiPublishApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {
        ResumeDetails obj = new ResumeDetails("topic", "dto");

        MqiPublishApiError e1 = new MqiPublishApiError(
                ProductCategory.EDRS_SESSIONS, obj, "error-message");
        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals(obj, e1.getOutput());
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.MQI_PUBLISH_API_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[output " + obj.toString() + "]"));
        assertTrue(str1.contains("[msg error-message]"));

        MqiPublishApiError e2 =
                new MqiPublishApiError(ProductCategory.EDRS_SESSIONS, obj,
                        "error-message", new Exception("cause-message"));
        assertEquals(ProductCategory.EDRS_SESSIONS, e2.getCategory());
        assertEquals(obj, e2.getOutput());
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.MQI_PUBLISH_API_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
