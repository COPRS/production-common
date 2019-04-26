package esa.s1pdgs.cpoc.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;

/**
 * Test the class MqiNextApiError
 * 
 * @author Viveris Technologies
 */
public class MqiNextApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {
        MqiNextApiError e1 = new MqiNextApiError(ProductCategory.EDRS_SESSIONS,
                "error-message");
        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.MQI_NEXT_API_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[msg error-message]"));
        
        MqiNextApiError e2 = new MqiNextApiError(ProductCategory.EDRS_SESSIONS,
                "error-message", new Exception("cause-message"));
        assertEquals(ProductCategory.EDRS_SESSIONS, e2.getCategory());
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.MQI_NEXT_API_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
