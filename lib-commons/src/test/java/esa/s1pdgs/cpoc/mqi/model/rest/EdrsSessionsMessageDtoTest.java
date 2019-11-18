package esa.s1pdgs.cpoc.mqi.model.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
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
        IngestionEvent body = new IngestionEvent("key-obs", 2,
                EdrsSessionFileType.RAW, "S1", "B", "WILE", "sessionId");
        
        GenericMessageDto<IngestionEvent> dto = new GenericMessageDto<IngestionEvent>(123, "input-key", body);
        assertEquals(123, dto.getId());
        assertEquals(body, dto.getBody());
        assertEquals("input-key", dto.getInputKey());

        dto = new GenericMessageDto<IngestionEvent>();
        dto.setId(321);
        dto.setBody(body);
        dto.setInputKey("othey-input");
        assertEquals(321, dto.getId());
        assertEquals(body, dto.getBody());
        assertEquals("othey-input", dto.getInputKey());
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        IngestionEvent body = new IngestionEvent("key-obs", 2,
                EdrsSessionFileType.RAW, "S1", "B", "WILE", "sessionId");
        GenericMessageDto<IngestionEvent> dto = new GenericMessageDto<IngestionEvent>(123, "input-key", body);
        String str = dto.toString();
        assertTrue("toString should contain the identifier",
                str.contains("id: 123"));
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
        EqualsVerifier.forClass(GenericMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
