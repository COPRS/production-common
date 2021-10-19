package esa.s1pdgs.cpoc.common.errors.appcatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class AppCatalogMqiAckApiError
 * 
 * @author Viveris Technologies
 */
public class AppCatalogMqiAckApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {
        ResumeDetails obj = new ResumeDetails("topic", "dto");

        AppCatalogMqiAckApiError e1 = new AppCatalogMqiAckApiError(
                ProductCategory.EDRS_SESSIONS, "uri-mqi", obj, "error-message");
        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals("uri-mqi", e1.getUri());
        assertEquals(obj, e1.getDto());
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.APPCATALOG_MQI_ACK_API_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[uri uri-mqi]"));
        assertTrue(str1.contains("[dto " + obj.toString() + "]"));
        assertTrue(str1.contains("[msg error-message]"));

        AppCatalogMqiAckApiError e2 = new AppCatalogMqiAckApiError(
                ProductCategory.EDRS_SESSIONS, "uri-mqi", obj, "error-message",
                new Exception("cause-message"));
        assertEquals(ProductCategory.EDRS_SESSIONS, e2.getCategory());
        assertEquals("uri-mqi", e1.getUri());
        assertEquals(obj, e1.getDto());
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.APPCATALOG_MQI_ACK_API_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
