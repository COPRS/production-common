package esa.s1pdgs.cpoc.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class MqiRouteNotAvailable
 * 
 * @author Viveris Technologies
 */
public class MqiRouteNotAvailableTest {

    /**
     * Test the KafkaSendException
     */
    @Test
    public void test() {
        MqiRouteNotAvailable e1 = new MqiRouteNotAvailable(
                ProductCategory.EDRS_SESSIONS, ProductFamily.AUXILIARY_FILE);

        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals(ProductFamily.AUXILIARY_FILE, e1.getFamily());
        assertEquals(ErrorCode.MQI_ROUTE_NOT_AVAILABLE, e1.getCode());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[family AUXILIARY_FILE]"));
    }

}
