package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object MqiEdrsSessionMessageDto
 * 
 * @author Viveris Technologies
 */
public class MqiEdrsSessionMessageDtoTest {

    /**
     * DTO
     */
    private EdrsSessionDto dto = new EdrsSessionDto("obs-key", 1,
            EdrsSessionFileType.RAW, "S1", "A");

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        MqiEdrsSessionMessageDto obj = new MqiEdrsSessionMessageDto();
        assertNull(obj.getDto());
        assertEquals(ProductCategory.EDRS_SESSIONS, obj.getCategory());

        MqiEdrsSessionMessageDto obj2 =
                new MqiEdrsSessionMessageDto(1000, "topic-name", 2, 3210);
        assertNull(obj2.getDto());
        assertEquals(ProductCategory.EDRS_SESSIONS, obj2.getCategory());
        assertEquals(1000, obj2.getIdentifier());
        assertEquals("topic-name", obj2.getTopic());
        assertEquals(2, obj2.getPartition());
        assertEquals(3210, obj2.getOffset());

        MqiEdrsSessionMessageDto obj3 =
                new MqiEdrsSessionMessageDto(1000, "topic-name", 2, 3210, dto);
        assertEquals(ProductCategory.EDRS_SESSIONS, obj3.getCategory());
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
        MqiEdrsSessionMessageDto obj =
                new MqiEdrsSessionMessageDto(1000, "topic-name", 2, 3210, dto);
        String str = obj.toString();
        assertTrue(str.contains(obj.toStringForExtend()));
        assertTrue(str.contains("dto: " + dto.toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(MqiEdrsSessionMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
