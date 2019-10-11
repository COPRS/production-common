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
	public void testAux() {
		ProductDto dto = new ProductDto("product-name", "key-obs", ProductFamily.AUXILIARY_FILE, null);
		assertEquals("product-name", dto.getProductName());
		assertEquals("key-obs", dto.getKeyObjectStorage());

		dto = new ProductDto();
		dto.setProductName("other-product");
		dto.setKeyObjectStorage("other-key");
		assertEquals("other-product", dto.getProductName());
		assertEquals("other-key", dto.getKeyObjectStorage());
	}
	
    @Test
    public void testLevelProduct() {
        ProductDto dto = new ProductDto("product-name", "key-obs", ProductFamily.L0_SLICE, "NRT");
        assertEquals("product-name", dto.getProductName());
        assertEquals("key-obs", dto.getKeyObjectStorage());
        assertEquals(ProductFamily.L0_SLICE, dto.getFamily());
        assertEquals("NRT", dto.getMode());
    }

    
    @Test
    public void testLevelSegment() {
        ProductDto dto = new ProductDto("product-name", "key-obs", ProductFamily.L0_SLICE, "NRT");
        assertEquals("product-name", dto.getProductName());
        assertEquals("key-obs", dto.getKeyObjectStorage());
        assertEquals(ProductFamily.L0_SLICE, dto.getFamily());
        assertEquals("NRT", dto.getMode());
    }
    
	/**
	 * Test the toString function
	 */
	@Test
	public void testToString() {
		ProductDto dto = new ProductDto("product-name", "key-obs", ProductFamily.AUXILIARY_FILE, null);
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
	
    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
        ProductDto dto = new ProductDto();
        dto.setProductName("product-name");
        dto.setKeyObjectStorage("key-obs");
        dto.setFamily(ProductFamily.L1_SLICE);
        dto.setMode("FAST");
        String str = dto.toString();
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.contains("keyObjectStorage: key-obs"));
        assertTrue(str.contains("family: L1_SLICE"));
        assertTrue(str.contains("mode: FAST"));
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSettersL2() {
        ProductDto dto = new ProductDto();
        dto.setProductName("product-name");
        dto.setKeyObjectStorage("key-obs");
        dto.setFamily(ProductFamily.L2_SLICE);
        dto.setMode("FAST");
        dto.setOcqFlag(OQCFlag.CHECKED_NOK);
        String str = dto.toString();
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.contains("keyObjectStorage: key-obs"));
        assertTrue(str.contains("family: L2_SLICE"));
        assertTrue(str.contains("mode: FAST"));
        assertTrue(str.contains("oqcFlag: CHECKED_NOK"));
    }


}
