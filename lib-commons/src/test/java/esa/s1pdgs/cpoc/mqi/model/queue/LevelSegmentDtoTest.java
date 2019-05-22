package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class L0SliceDto
 * 
 * @author Viveris Technologies
 */
public class LevelSegmentDtoTest {

    /**
     * Test default cosntructor and getters
     */
    @Test
    public void testConstructor() {
        LevelSegmentDto dto = new LevelSegmentDto("product-name", "key-obs",
                ProductFamily.L0_SLICE, "NRT");
        assertEquals("product-name", dto.getName());
        assertEquals("key-obs", dto.getKeyObs());
        assertEquals(ProductFamily.L0_SLICE, dto.getFamily());
        assertEquals("NRT", dto.getMode());
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
        LevelSegmentDto dto = new LevelSegmentDto();
        dto.setName("product-name");
        dto.setKeyObs("key-obs");
        dto.setFamily(ProductFamily.L1_SLICE);
        dto.setMode("fast");
        String str = dto.toString();
        assertTrue(str.contains("name: product-name"));
        assertTrue(str.contains("keyObs: key-obs"));
        assertTrue(str.contains("family: L1_SLICE"));
        assertTrue(str.contains("mode: FAST"));
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSettersL2() {
        LevelSegmentDto dto = new LevelSegmentDto();
        dto.setName("product-name");
        dto.setKeyObs("key-obs");
        dto.setFamily(ProductFamily.L2_SLICE);
        dto.setMode("fast");
        String str = dto.toString();
        assertTrue(str.contains("name: product-name"));
        assertTrue(str.contains("keyObs: key-obs"));
        assertTrue(str.contains("family: L2_SLICE"));
        assertTrue(str.contains("mode: FAST"));
    }
    
    /**
     * Test equals and hashcode
     */
    @Test
    public void equalsDto() {
        EqualsVerifier.forClass(LevelSegmentDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
