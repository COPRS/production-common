package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object MqiLevelJobMessageDto
 * 
 * @author Viveris Technologies
 */
public class MqiLevelJobMessageDtoTest {

    /**
     * DTO
     */
    private LevelJobDto dto = new LevelJobDto(ProductFamily.L1_JOB,
            "prodcut-name", "NRT", "work-dir", "job-order");

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        MqiLevelJobMessageDto obj = new MqiLevelJobMessageDto();
        assertNull(obj.getDto());
        assertEquals(ProductCategory.LEVEL_JOBS, obj.getCategory());

        MqiLevelJobMessageDto obj2 =
                new MqiLevelJobMessageDto(1000, "topic-name", 2, 3210);
        assertNull(obj2.getDto());
        assertEquals(ProductCategory.LEVEL_JOBS, obj2.getCategory());
        assertEquals(1000, obj2.getIdentifier());
        assertEquals("topic-name", obj2.getTopic());
        assertEquals(2, obj2.getPartition());
        assertEquals(3210, obj2.getOffset());

        MqiLevelJobMessageDto obj3 =
                new MqiLevelJobMessageDto(1000, "topic-name", 2, 3210, dto);
        assertEquals(ProductCategory.LEVEL_JOBS, obj3.getCategory());
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
        MqiLevelJobMessageDto obj =
                new MqiLevelJobMessageDto(1000, "topic-name", 2, 3210, dto);
        String str = obj.toString();
        assertTrue(str.contains(obj.toStringForExtend()));
        assertTrue(str.contains("dto: " + dto.toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(MqiLevelJobMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
