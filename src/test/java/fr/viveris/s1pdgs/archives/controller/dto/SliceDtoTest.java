package fr.viveris.s1pdgs.archives.controller.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.archives.model.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class SliceDtoTest {

    /**
     * Test default cosntructor and getters
     */
    @Test
    public void testConstructor() {
        SliceDto dto = new SliceDto("product-name", "key-obs",
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
        SliceDto dto = new SliceDto("product-name", "key-obs",
                ProductFamily.L0_PRODUCT);
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
        EqualsVerifier.forClass(SliceDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
