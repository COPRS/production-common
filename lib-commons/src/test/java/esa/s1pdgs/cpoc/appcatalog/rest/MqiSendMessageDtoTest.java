package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object MqiSendMessageDto
 * 
 * @author Viveris Technologies
 */
public class MqiSendMessageDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        MqiSendMessageDto obj = new MqiSendMessageDto();
        assertFalse(obj.isForce());
        obj.setPod("pod-name");
        obj.setForce(true);
        assertEquals("pod-name", obj.getPod());
        assertTrue(obj.isForce());

        MqiSendMessageDto obj1 = new MqiSendMessageDto("pod-name", true);
        assertEquals("pod-name", obj1.getPod());
        assertTrue(obj.isForce());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        MqiSendMessageDto obj = new MqiSendMessageDto("pod-name", true);
        String str = obj.toString();
        assertTrue(str.contains("pod: pod-name"));
        assertTrue(str.contains("force: true"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(MqiSendMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
