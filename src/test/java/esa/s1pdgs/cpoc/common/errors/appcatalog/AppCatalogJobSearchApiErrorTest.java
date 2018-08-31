package esa.s1pdgs.cpoc.common.errors.appcatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class AppCatalogMqiGetOffsetApiError
 * 
 * @author Viveris Technologies
 */
public class AppCatalogJobSearchApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {

        AppCatalogJobSearchApiError e1 =
                new AppCatalogJobSearchApiError("uri-mqi", "error-message");
        assertEquals("uri-mqi", e1.getUri());
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.APPCATALOG_JOB_SEARCH_API_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[uri uri-mqi]"));
        assertTrue(str1.contains("[msg error-message]"));

        AppCatalogJobSearchApiError e2 = new AppCatalogJobSearchApiError(
                "uri-mqi", "error-message", new Exception("cause-message"));
        assertEquals("uri-mqi", e2.getUri());
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.APPCATALOG_JOB_SEARCH_API_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
