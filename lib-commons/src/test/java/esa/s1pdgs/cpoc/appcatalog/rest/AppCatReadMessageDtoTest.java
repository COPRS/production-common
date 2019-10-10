package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object MqiGenericReadMessage
 * 
 * @author Viveris Technologies
 */
public class AppCatReadMessageDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppCatReadMessageDto<String> obj = new AppCatReadMessageDto<>();
        assertFalse(obj.isForce());
        obj.setGroup("group");
        obj.setPod("pod-name");
        obj.setForce(true);
        obj.setDto("dto-obj");
        assertEquals("group", obj.getGroup());
        assertEquals("pod-name", obj.getPod());
        assertEquals("dto-obj", obj.getDto());
        assertTrue(obj.isForce());

        AppCatReadMessageDto<String> obj1 =
                new AppCatReadMessageDto<String>("group", "pod-name", true,
                        "dto-obj");
        assertEquals("group", obj1.getGroup());
        assertEquals("pod-name", obj1.getPod());
        assertEquals("dto-obj", obj1.getDto());
        assertTrue(obj.isForce());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        AppCatReadMessageDto<String> obj =
                new AppCatReadMessageDto<String>("group", "pod-name", true,
                        "dto-obj");
        String str = obj.toString();
        assertTrue(str.contains("group: group"));
        assertTrue(str.contains("pod: pod-name"));
        assertTrue(str.contains("force: true"));
        assertTrue(str.contains("dto: dto-obj"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppCatReadMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
