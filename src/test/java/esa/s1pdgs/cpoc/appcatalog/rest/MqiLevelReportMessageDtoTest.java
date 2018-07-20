package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object MqiLevelReportMessageDto
 * 
 * @author Viveris Technologies
 */
public class MqiLevelReportMessageDtoTest {

    /**
     * DTO
     */
    private LevelReportDto dto = new LevelReportDto("prodcut-name", "key-obs",
            ProductFamily.L1_REPORT);

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        MqiLevelReportMessageDto obj = new MqiLevelReportMessageDto();
        assertNull(obj.getDto());
        assertEquals(ProductCategory.LEVEL_REPORTS, obj.getCategory());

        MqiLevelReportMessageDto obj2 =
                new MqiLevelReportMessageDto(1000, "topic-name", 2, 3210);
        assertNull(obj2.getDto());
        assertEquals(ProductCategory.LEVEL_REPORTS, obj2.getCategory());
        assertEquals(1000, obj2.getIdentifier());
        assertEquals("topic-name", obj2.getTopic());
        assertEquals(2, obj2.getPartition());
        assertEquals(3210, obj2.getOffset());

        MqiLevelReportMessageDto obj3 =
                new MqiLevelReportMessageDto(1000, "topic-name", 2, 3210, dto);
        assertEquals(ProductCategory.LEVEL_REPORTS, obj3.getCategory());
        assertEquals(1000, obj3.getIdentifier());
        assertEquals("topic-name", obj3.getTopic());
        assertEquals(2, obj3.getPartition());
        assertEquals(3210, obj3.getOffset());
        assertEquals(dto, obj3.getDto());

    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        MqiLevelReportMessageDto obj =
                new MqiLevelReportMessageDto(1000, "topic-name", 2, 3210, dto);
        String str = obj.toString();
        assertTrue(str.contains(obj.toStringForExtend()));
        assertTrue(str.contains("dto: " + dto.toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(MqiLevelReportMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
