/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.appcatalog.server.model.MqiMessage;
import esa.s1pdgs.cpoc.common.ProductCategory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test class for MqiMessage
 * 
 * @author Viveris Technologies
 */
public class MqiMessageTest {
    
    @Test
    public void testGetter() {
        Date now = new Date();
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", now, 
                "sendingPod", now, now, 0, null);
        assertEquals(ProductCategory.AUXILIARY_FILES, message.getCategory());
        assertEquals("topic", message.getTopic());
        assertEquals(1, message.getPartition());
        assertEquals(5, message.getOffset());
        assertEquals("group", message.getGroup());
        assertEquals(MqiStateMessageEnum.READ, message.getState());
        assertEquals("readingPod", message.getReadingPod());
        assertEquals(now, message.getLastAckDate());
        assertEquals(now, message.getLastReadDate());
        assertEquals(now, message.getLastSendDate());
        assertEquals("sendingPod", message.getSendingPod());
        assertEquals(0, message.getNbRetries());
        assertEquals(null, message.getDto());
    }
    
    @Test
    public void testToString() {
        Date now = new Date();
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", now, 
                "sendingPod", now, now, 0, null);
        message = new MqiMessage();
        message.setCategory(ProductCategory.AUXILIARY_FILES);
        message.setTopic("topic");
        message.setPartition(1);
        message.setOffset(5);
        message.setGroup("group");
        message.setState(MqiStateMessageEnum.READ);
        message.setReadingPod("readingPod");
        message.setLastAckDate(now);
        message.setLastReadDate(now);
        message.setLastSendDate(now);
        message.setSendingPod("sendingPod");
        message.setNbRetries(0);
        message.setDto(null);
        
        String str = message.toString();
        assertTrue(str.contains("category\":\"AUXILIARY_FILES"));
        assertTrue(str.contains("topic\":\"topic"));
        assertTrue(str.contains("partition\":\"1"));
        assertTrue(str.contains("offset\":\"5"));
        assertTrue(str.contains("group\":\"group"));
        assertTrue(str.contains("state\":\"READ"));
        assertTrue(str.contains("readingPod\":\"readingPod"));
        assertTrue(str.contains("lastAckDate\":\""+now));
        assertTrue(str.contains("lastReadDate\":\""+now));
        assertTrue(str.contains("lastSendDate\":\""+now));
        assertTrue(str.contains("sendingPod\":\"sendingPod"));
        assertTrue(str.contains("nbRetries\":\"0"));
        assertTrue(str.contains("dto\":\"null"));
        assertNotNull(message.getIdentifier());
    }
    
    /**
     * Test class for hashcode and equals
     */
    @Test
    public void testEqualsMqiMessage() {
        EqualsVerifier.forClass(MqiMessage.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
