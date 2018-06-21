package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

/**
 * Test the DTO L1SliceDto
 * 
 * @author Viveris Technologies
 */
public class L1SliceDtoTest {

    /**
     * Test the constructor
     */
    @Test
    public void testConstructor() {
        L1SliceDto dto = new L1SliceDto("product-name", "key-obs");
        assertEquals("product-name", dto.getProductName());
        assertEquals("key-obs", dto.getKeyObjectStorage());
        assertEquals(ProductFamily.L1_PRODUCT, dto.getFamily());
    }
}
