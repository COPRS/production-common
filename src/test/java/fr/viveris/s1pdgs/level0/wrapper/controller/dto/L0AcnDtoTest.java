package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

/**
 * Test the DTO L0AcnDto
 * 
 * @author Viveris Technologies
 */
public class L0AcnDtoTest {

    /**
     * Test the constructor
     */
    @Test
    public void testConstructor() {
        L0AcnDto dto = new L0AcnDto("product-name", "key-obs");
        assertEquals("product-name", dto.getProductName());
        assertEquals("key-obs", dto.getKeyObjectStorage());
        assertEquals(ProductFamily.L0_ACN, dto.getFamily());
    }
}
