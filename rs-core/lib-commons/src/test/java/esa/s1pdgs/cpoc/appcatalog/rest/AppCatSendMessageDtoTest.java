package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object AppCatSendMessageDto
 * 
 * @author Viveris Technologies
 */
public class AppCatSendMessageDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppCatSendMessageDto obj = new AppCatSendMessageDto();
        assertFalse(obj.isForce());
        obj.setPod("pod-name");
        obj.setForce(true);
        assertEquals("pod-name", obj.getPod());
        assertTrue(obj.isForce());

        AppCatSendMessageDto obj1 = new AppCatSendMessageDto("pod-name", true);
        assertEquals("pod-name", obj1.getPod());
        assertTrue(obj.isForce());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        AppCatSendMessageDto obj = new AppCatSendMessageDto("pod-name", true);
        String str = obj.toString();
        assertTrue(str.contains("pod: pod-name"));
        assertTrue(str.contains("force: true"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppCatSendMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
