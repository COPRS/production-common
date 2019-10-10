package esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Test the enum ProductFamily
 * @author Cyrielle Gailliard
 *
 */
public class ProductFamilyTest {
    
    /**
     * Test basic enumeration
     */
    @Test
    public void testBasic() {
        assertEquals(ProductFamily.L0_SLICE, ProductFamily.valueOf("L0_SLICE"));
    }

	/**
	 * Test the function fromValue
	 */
	@Test
	public void testFromVamlue() {
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue(null));
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue(""));
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue("edrs_session"));
		assertEquals(ProductFamily.EDRS_SESSION, ProductFamily.fromValue("EDRS_SESSION"));
		assertEquals(ProductFamily.L1_REPORT, ProductFamily.fromValue("L1_REPORT"));
		assertEquals(ProductFamily.L2_REPORT, ProductFamily.fromValue("L2_REPORT"));
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue("L1_REPORTd"));
	}
}
