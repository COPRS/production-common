package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Viveris Technologies
 *
 */
public class ProductDtoTest {

	/**
	 * Test getters, setters and constructors
	 */
	@Test
	public void testGettersSettersConstructors() {
		ProductDto dto = new ProductDto("product-name", "key-obs", ProductFamily.AUXILIARY_FILE);
		assertEquals("product-name", dto.getProductName());
		assertEquals("key-obs", dto.getKeyObjectStorage());

		dto = new ProductDto();
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
		ProductDto dto = new ProductDto("product-name", "key-obs", ProductFamily.AUXILIARY_FILE);
		String str = dto.toString();
		assertTrue("toString should contain the product name", str.contains("productName: product-name"));
		assertTrue("toString should contain the key OBS", str.contains("keyObjectStorage: key-obs"));
	}

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(ProductDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
