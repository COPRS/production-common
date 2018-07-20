package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class L0SliceDto
 * 
 * @author Viveris Technologies
 */
public class LevelProductDtoTest {

    /**
     * Test default cosntructor and getters
     */
    @Test
    public void testConstructor() {
        LevelProductDto dto = new LevelProductDto("product-name", "key-obs",
                ProductFamily.L0_PRODUCT);
        assertEquals("product-name", dto.getProductName());
        assertEquals("key-obs", dto.getKeyObjectStorage());
        assertEquals(ProductFamily.L0_PRODUCT, dto.getFamily());
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
        LevelProductDto dto = new LevelProductDto();
        dto.setProductName("product-name");
        dto.setKeyObjectStorage("key-obs");
        dto.setFamily(ProductFamily.L1_PRODUCT);
        String str = dto.toString();
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.contains("keyObjectStorage: key-obs"));
        assertTrue(str.contains("family: L1_PRODUCT"));
    }

    /**
     * Test equals and hashcode
     */
    @Test
    public void equalsDto() {
        EqualsVerifier.forClass(LevelProductDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
