package fr.viveris.s1pdgs.level0.wrapper.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the enumeration ProductFamily
 * 
 * @author Viveris Technologies
 */
public class ProductFamilyTest {

    /**
     * Test default method of enumeration
     */
    @Test
    public void testValueOf() {
        assertEquals(10, ProductFamily.values().length);
        assertEquals(ProductFamily.BLANK, ProductFamily.valueOf("BLANK"));
    }

    /**
     * test fromValue
     */
    @Test
    public void testFromValue() {
        assertEquals(ProductFamily.BLANK, ProductFamily.fromValue("tutu"));
        assertEquals(ProductFamily.CONFIG, ProductFamily.fromValue("CONFIG"));
        assertEquals(ProductFamily.JOB, ProductFamily.fromValue("JOB"));
        assertEquals(ProductFamily.L0_ACN, ProductFamily.fromValue("L0_ACN"));
        assertEquals(ProductFamily.L0_PRODUCT,
                ProductFamily.fromValue("L0_PRODUCT"));
        assertEquals(ProductFamily.L0_REPORT,
                ProductFamily.fromValue("L0_REPORT"));
        assertEquals(ProductFamily.L1_ACN, ProductFamily.fromValue("L1_ACN"));
        assertEquals(ProductFamily.L1_PRODUCT,
                ProductFamily.fromValue("L1_PRODUCT"));
        assertEquals(ProductFamily.L1_REPORT,
                ProductFamily.fromValue("L1_REPORT"));
    }
}
