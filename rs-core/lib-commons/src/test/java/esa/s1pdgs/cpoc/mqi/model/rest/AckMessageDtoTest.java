package esa.s1pdgs.cpoc.mqi.model.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object AckMessageDto
 * 
 * @author Viveris Technologies
 */
public class AckMessageDtoTest {

    /**
     * Test getters, setters and constructors
     */
    @Test
    public void testGettersSettersConstructors() {
        AckMessageDto dto = new AckMessageDto(123, Ack.ERROR, "ack-message", true);
        assertEquals(123, dto.getMessageId());
        assertEquals(Ack.ERROR, dto.getAck());
        assertEquals("ack-message", dto.getMessage());
        assertTrue(dto.isStop());

        dto = new AckMessageDto();
        dto.setMessageId(321);
        dto.setAck(Ack.OK);
        dto.setMessage("other-message");
        dto.setStop(true);
        assertEquals(321, dto.getMessageId());
        assertEquals(Ack.OK, dto.getAck());
        assertEquals("other-message", dto.getMessage());
        assertTrue(dto.isStop());
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        AckMessageDto dto = new AckMessageDto(123, Ack.ERROR, "ack-message", true);
        String str = dto.toString();
        assertTrue("toString should contain the identifier",
                str.contains("messageId: 123"));
        assertTrue("toString should contain the ack",
                str.contains("ack: ERROR"));
        assertTrue("toString should contain the message",
                str.contains("message: ack-message"));
        assertTrue("toString should contain stop",
                str.contains("stop: true"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(AckMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
