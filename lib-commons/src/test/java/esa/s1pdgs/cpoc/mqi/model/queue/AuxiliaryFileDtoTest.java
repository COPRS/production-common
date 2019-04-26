package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Viveris Technologies
 *
 */
public class AuxiliaryFileDtoTest {

	/**
	 * Test getters, setters and constructors
	 */
	@Test
	public void testGettersSettersConstructors() {
		AuxiliaryFileDto dto = new AuxiliaryFileDto("product-name", "key-obs");
		assertEquals("product-name", dto.getProductName());
		assertEquals("key-obs", dto.getKeyObjectStorage());

		dto = new AuxiliaryFileDto();
		dto.setProductName("other-product");
		dto.setKeyObjectStorage("other-key");
		assertEquals("other-product", dto.getProductName());
		assertEquals("other-key", dto.getKeyObjectStorage());
	}

	/**
	 * Test the toString function
	 */
	@Test
	public void testToString() {
		AuxiliaryFileDto dto = new AuxiliaryFileDto("product-name", "key-obs");
		String str = dto.toString();
		assertTrue("toString should contain the product name", str.contains("productName: product-name"));
		assertTrue("toString should contain the key OBS", str.contains("keyObjectStorage: key-obs"));
	}

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(AuxiliaryFileDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
