package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object MqiLightMessageDto
 * 
 * @author Viveris Technologies
 */
public class MqiLightMessageDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        MqiLightMessageDto obj = new MqiLightMessageDto();
        assertEquals(MqiStateMessageEnum.READ, obj.getState());

        MqiLightMessageDto obj1 =
                new MqiLightMessageDto(ProductCategory.AUXILIARY_FILES);
        assertEquals(ProductCategory.AUXILIARY_FILES, obj1.getCategory());

        MqiLightMessageDto obj2 = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1000, "topic-name", 2, 3210);
        assertEquals(ProductCategory.AUXILIARY_FILES, obj2.getCategory());
        assertEquals(1000, obj2.getIdentifier());
        assertEquals("topic-name", obj2.getTopic());
        assertEquals(2, obj2.getPartition());
        assertEquals(3210, obj2.getOffset());

        MqiLightMessageDto obj3 = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1000, "topic-name", 2, 3210);
        assertEquals(ProductCategory.AUXILIARY_FILES, obj3.getCategory());
        assertEquals(1000, obj3.getIdentifier());
        assertEquals("topic-name", obj3.getTopic());
        assertEquals(2, obj3.getPartition());
        assertEquals(3210, obj3.getOffset());

        obj3.setGroup("group-name");
        obj3.setLastAckDate(new Date(888888888));
        obj3.setLastReadDate(new Date(999999999));
        obj3.setLastSendDate(new Date(777777777));
        obj3.setNbRetries(3);
        obj3.setReadingPod("reading-pod");
        obj3.setSendingPod("sending-pod");
        obj3.setState(MqiStateMessageEnum.ACK_WARN);
        assertEquals("group-name", obj3.getGroup());
        assertEquals(new Date(888888888), obj3.getLastAckDate());
        assertEquals(new Date(999999999), obj3.getLastReadDate());
        assertEquals(new Date(777777777), obj3.getLastSendDate());
        assertEquals(3, obj3.getNbRetries());
        assertEquals("reading-pod", obj3.getReadingPod());
        assertEquals("sending-pod", obj3.getSendingPod());
        assertEquals(MqiStateMessageEnum.ACK_WARN, obj3.getState());

    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        MqiLightMessageDto obj = new MqiLightMessageDto();
        obj.setCategory(ProductCategory.EDRS_SESSIONS);
        obj.setTopic("topic-name");
        obj.setIdentifier(1000);
        obj.setPartition(2);
        obj.setOffset(3210);
        obj.setGroup("group-name");
        obj.setLastAckDate(new Date(888888888));
        obj.setLastReadDate(new Date(999999999));
        obj.setLastSendDate(new Date(777777777));
        obj.setNbRetries(3);
        obj.setReadingPod("reading-pod");
        obj.setSendingPod("sending-pod");
        obj.setState(MqiStateMessageEnum.ACK_WARN);

        String str = obj.toString();
        assertTrue(str.contains("category: EDRS_SESSIONS"));
        assertTrue(str.contains("identifier: 1000"));
        assertTrue(str.contains("topic: topic-name"));
        assertTrue(str.contains("partition: 2"));
        assertTrue(str.contains("offset: 3210"));
        assertTrue(str.contains("group: group-name"));
        assertTrue(str.contains("state: ACK_WARN"));
        assertTrue(str.contains("readingPod: reading-pod"));
        assertTrue(str.contains("sendingPod: sending-pod"));
        assertTrue(str.contains("nbRetries: 3"));
        assertTrue(str
                .contains("lastReadDate: " + (new Date(999999999)).toString()));
        assertTrue(str
                .contains("lastAckDate: " + (new Date(888888888)).toString()));
        assertTrue(str
                .contains("lastSendDate: " + (new Date(777777777)).toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(MqiLightMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
