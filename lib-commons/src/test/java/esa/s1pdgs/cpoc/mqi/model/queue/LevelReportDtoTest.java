
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
public class LevelReportDtoTest {

    /**
     * Test default cosntructor and getters
     */
    @Test
    public void testConstructor() {
        final LevelReportDto dto = new LevelReportDto("product-name", "key-obs",
                ProductFamily.L0_REPORT);
        assertEquals("product-name", dto.getKeyObjectStorage());
        assertEquals("key-obs", dto.getContent());
        assertEquals(ProductFamily.L0_REPORT, dto.getProductFamily());
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
        final LevelReportDto dto = new LevelReportDto();
        dto.setKeyObjectStorage("product-name");
        dto.setContent("key-obs");
        dto.setProductFamily(ProductFamily.L1_REPORT);
        final String str = dto.toString();
        assertTrue(str.contains("product-name"));
        assertTrue(str.contains("key-obs"));
        assertTrue(str.contains("L1_REPORT"));
    }

    /**
     * Test equals and hashcode
     */
    @Test
    public void equalsDto() {
        EqualsVerifier.forClass(LevelReportDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
