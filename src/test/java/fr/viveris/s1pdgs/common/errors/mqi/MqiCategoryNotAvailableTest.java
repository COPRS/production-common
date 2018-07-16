package fr.viveris.s1pdgs.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class KafkaSendException
 * 
 * @author Viveris Technologies
 */
public class MqiCategoryNotAvailableTest {

    /**
     * Test the KafkaSendException
     */
    @Test
    public void test() {
        MqiCategoryNotAvailable e1 = new MqiCategoryNotAvailable(ProductCategory.EDRS_SESSIONS,
                "consumer");

        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals("consumer", e1.getType());
        assertEquals(ErrorCode.MQI_CATEGORY_NOT_AVAILABLE, e1.getCode());
        assertEquals("No consumer available for category EDRS_SESSIONS", e1.getMessage());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[msg No consumer available for category EDRS_SESSIONS]"));
    }

}
