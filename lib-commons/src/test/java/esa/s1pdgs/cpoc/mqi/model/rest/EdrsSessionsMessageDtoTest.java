package esa.s1pdgs.cpoc.mqi.model.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object EdrsSessionsMessageDto
 * 
 * @author Viveris Technologies
 */
public class EdrsSessionsMessageDtoTest {

    /**
     * Test getters, setters and constructors
     */
    @Test
    public void testGettersSettersConstructors() {
        EdrsSessionDto body = new EdrsSessionDto("key-obs", 2,
                EdrsSessionFileType.RAW, "S1", "B");
        EdrsSessionsMessageDto dto =
                new EdrsSessionsMessageDto(123, "input-key", body);
        assertEquals(123, dto.getIdentifier());
        assertEquals(body, dto.getBody());
        assertEquals("input-key", dto.getInputKey());

        dto = new EdrsSessionsMessageDto();
        dto.setIdentifier(321);
        dto.setBody(body);
        dto.setInputKey("othey-input");
        assertEquals(321, dto.getIdentifier());
        assertEquals(body, dto.getBody());
        assertEquals("othey-input", dto.getInputKey());
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        EdrsSessionDto body = new EdrsSessionDto("key-obs", 2,
                EdrsSessionFileType.RAW, "S1", "B");
        EdrsSessionsMessageDto dto =
                new EdrsSessionsMessageDto(123, "input-key", body);
        String str = dto.toString();
        assertTrue("toString should contain the identifier",
                str.contains("identifier: 123"));
        assertTrue("toString should contain the body",
                str.contains("body: " + body.toString()));
        assertTrue("toString should contain the input key",
                str.contains("inputKey: input-key"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(EdrsSessionsMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
