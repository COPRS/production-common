package esa.s1pdgs.cpoc.ingestor.files.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestor.files.model.dto.KafkaConfigFileDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Cyrielle Gailliard
 *
 */
public class KafkaConfigFileDtoTest {

	/**
	 * Test getters, setters and constructors
	 */
	@Test
	public void testGettersSettersConstructors() {
		KafkaConfigFileDto dto = new KafkaConfigFileDto("product-name", "key-obs");
		assertEquals("product-name", dto.getProductName());
		assertEquals("key-obs", dto.getKeyObjectStorage());

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
		KafkaConfigFileDto dto = new KafkaConfigFileDto("product-name", "key-obs");
		String str = dto.toString();
		assertTrue("toString should contain the product name", str.contains("productName: product-name"));
		assertTrue("toString should contain the key OBS", str.contains("keyObjectStorage: key-obs"));
	}

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(KafkaConfigFileDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
