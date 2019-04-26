package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object MqiAuxiliaryFileMessageDto
 * 
 * @author Viveris Technologies
 */
public class MqiAuxiliaryFileMessageDtoTest {

    /**
     * DTO
     */
    private AuxiliaryFileDto dto =
            new AuxiliaryFileDto("prodcut-name", "key-obs");

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        MqiAuxiliaryFileMessageDto obj = new MqiAuxiliaryFileMessageDto();
        assertNull(obj.getDto());
        assertEquals(ProductCategory.AUXILIARY_FILES, obj.getCategory());

        MqiAuxiliaryFileMessageDto obj2 =
                new MqiAuxiliaryFileMessageDto(1000, "topic-name", 2, 3210);
        assertNull(obj2.getDto());
        assertEquals(ProductCategory.AUXILIARY_FILES, obj2.getCategory());
        assertEquals(1000, obj2.getIdentifier());
        assertEquals("topic-name", obj2.getTopic());
        assertEquals(2, obj2.getPartition());
        assertEquals(3210, obj2.getOffset());

        MqiAuxiliaryFileMessageDto obj3 = new MqiAuxiliaryFileMessageDto(1000,
                "topic-name", 2, 3210, dto);
        assertEquals(ProductCategory.AUXILIARY_FILES, obj3.getCategory());
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
        MqiAuxiliaryFileMessageDto obj = new MqiAuxiliaryFileMessageDto(1000,
                "topic-name", 2, 3210, dto);
        String str = obj.toString();
        assertTrue(str.contains(obj.toStringForExtend()));
        assertTrue(str.contains("dto: " + dto.toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(MqiAuxiliaryFileMessageDto.class)
                .usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
