package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiAuxiliaryFileMessageDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppDataJob obj = new AppDataJob();
        
        // check default constructor
        assertEquals(0, obj.getMessages().size());
        assertEquals(AppDataJobState.WAITING, obj.getState());
        assertNull(obj.getCreationDate());
        assertNull(obj.getLastUpdateDate());
        
        obj.setIdentifier(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.DISPATCHING);
        obj.setSessionId("session-id");
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        MqiAuxiliaryFileMessageDto message1 = new MqiAuxiliaryFileMessageDto(1, "topic1", 0, 12);
        MqiAuxiliaryFileMessageDto message2 = new MqiAuxiliaryFileMessageDto(2, "topic1", 1, 8);
        obj.setMessages(Arrays.asList(message1, message2));

        // check setters
        assertEquals(123, obj.getIdentifier());
        assertEquals(ApplicationLevel.L1, obj.getLevel());
        assertEquals(2, obj.getMessages().size());
        assertEquals(AppDataJobState.DISPATCHING, obj.getState());
        assertNotNull(obj.getCreationDate());
        assertNotNull(obj.getLastUpdateDate());
        assertEquals("pod-name", obj.getPod());
        assertEquals("session-id", obj.getSessionId());
        
        // check toString
        String str = obj.toString();
        assertTrue(str.contains("identifier: 123"));
        assertTrue(str.contains("level: L1"));
        assertTrue(str.contains("state: DISPATCHING"));
        assertTrue(str.contains("pod: pod-name"));
        assertTrue(str.contains("sessionId: session-id"));
        assertTrue(str.contains("creationDate: "));
        assertTrue(str.contains("lastUpdateDate: "));
        assertTrue(str.contains("messages: " + Arrays.asList(message1, message2).toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJob.class)
                .usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
