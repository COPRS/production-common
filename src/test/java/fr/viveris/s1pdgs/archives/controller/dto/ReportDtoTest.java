package fr.viveris.s1pdgs.archives.controller.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.archives.model.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * @author Viveris Technologies
 */
public class ReportDtoTest {

    /**
     * Test default constructor and getters
     */
    @Test
    public void testConstructor() {
        ReportDto dto = new ReportDto("product-name", "key-obs",
                ProductFamily.L0_REPORT);
        assertEquals("product-name", dto.getProductName());
        assertEquals("key-obs", dto.getContent());
        assertEquals(ProductFamily.L0_REPORT, dto.getFamily());
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
        ReportDto dto = new ReportDto("product-name", "key-obs",
                ProductFamily.L0_REPORT);
        dto.setProductName("product-name");
        dto.setContent("key-obs");
        dto.setFamily(ProductFamily.L1_REPORT);
        String str = dto.toString();
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.contains("content: key-obs"));
        assertTrue(str.contains("family: L1_REPORT"));
    }

    /**
     * Test equals and hashcode
     */
    @Test
    public void equalsDto() {
        EqualsVerifier.forClass(ReportDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
