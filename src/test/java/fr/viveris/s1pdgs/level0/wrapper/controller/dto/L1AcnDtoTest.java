package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

/**
 * Test the DTO L1AcnDto
 * 
 * @author Viveris Technologies
 */
public class L1AcnDtoTest {

    /**
     * Test the constructor
     */
    @Test
    public void testConstructor() {
        L1AcnDto dto = new L1AcnDto("product-name", "key-obs");
        assertEquals("product-name", dto.getProductName());
        assertEquals("key-obs", dto.getKeyObjectStorage());
        assertEquals(ProductFamily.L1_ACN, dto.getFamily());
    }
}
