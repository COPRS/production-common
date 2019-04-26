package esa.s1pdgs.cpoc.common.errors.appcatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class AppCatalogMqiGetOffsetApiError
 * 
 * @author Viveris Technologies
 */
public class AppCatalogMqiGetApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {

        AppCatalogMqiGetApiError e1 = new AppCatalogMqiGetApiError(
                ProductCategory.EDRS_SESSIONS, "uri-mqi", "error-message");
        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals("uri-mqi", e1.getUri());
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.APPCATALOG_MQI_GET_API_ERROR,
                e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[uri uri-mqi]"));
        assertTrue(str1.contains("[msg error-message]"));

        AppCatalogMqiGetApiError e2 = new AppCatalogMqiGetApiError(
                ProductCategory.EDRS_SESSIONS, "uri-mqi", "error-message",
                new Exception("cause-message"));
        assertEquals(ProductCategory.EDRS_SESSIONS, e2.getCategory());
        assertEquals("uri-mqi", e1.getUri());
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.APPCATALOG_MQI_GET_API_ERROR,
                e2.getCode());
        assertNotNull(e2.getCause());
    }

}
