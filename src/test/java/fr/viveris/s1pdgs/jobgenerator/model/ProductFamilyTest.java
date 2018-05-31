package fr.viveris.s1pdgs.jobgenerator.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue("raw"));
		assertEquals(ProductFamily.RAW, ProductFamily.fromValue("RAW"));
		assertEquals(ProductFamily.L1_REPORT, ProductFamily.fromValue("L1_REPORT"));
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue("L1_REPORTd"));
	}
}
