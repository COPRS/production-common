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
public class AppCatalogJobPatchApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {

        AppCatalogJobPatchApiError e1 = new AppCatalogJobPatchApiError(
                "uri-mqi", 1258L, "error-message");
        assertEquals("uri-mqi", e1.getUri());
        assertEquals(1258L, e1.getBody());
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.APPCATALOG_JOB_PATCH_API_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[uri uri-mqi]"));
        assertTrue(str1.contains("[body 1258]"));
        assertTrue(str1.contains("[msg error-message]"));

        AppCatalogJobPatchApiError e2 =
                new AppCatalogJobPatchApiError("uri-mqi", "toto",
                        "error-message", new Exception("cause-message"));
        assertEquals("uri-mqi", e2.getUri());
        assertEquals("toto", e2.getBody());
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.APPCATALOG_JOB_PATCH_API_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
