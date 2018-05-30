package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class L0SliceDto
 * @author Cyrielle
 *
 */
public class L0SliceDtoTest {

	/**
	 * Test default cosntructor and getters
	 */
	@Test
	public void testConstructor() {
		L0SliceDto dto = new L0SliceDto("product-name", "key-obs");
		assertEquals("product-name", dto.getProductName());
		assertEquals("key-obs", dto.getKeyObjectStorage());
	}

	/**
	 * Test toString methods and setters
	 */
	@Test
	public void testToStringAndSetters() {
		L0SliceDto dto = new L0SliceDto();
		dto.setProductName("product-name");
		dto.setKeyObjectStorage("key-obs");
		String str = dto.toString();
		assertTrue("toString should contains product name", str.contains("productName: product-name"));
		assertTrue("toString should contains product name", str.contains("keyObjectStorage: key-obs"));
	}

	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(L0SliceDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
