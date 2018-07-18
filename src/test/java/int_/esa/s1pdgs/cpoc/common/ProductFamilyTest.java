package int_.esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import int_.esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Test the enum ProductFamily
 * @author Cyrielle Gailliard
 *
 */
public class ProductFamilyTest {

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
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue("L1_REPORTd"));
	}
}
