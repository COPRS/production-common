package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.MessageState;

/**
 * Test the enumeration MessageState
 * 
 * @author Viveris Technologies
 */
public class MqiStateMessageEnumTest {

    /**
     * Test values and valueOf
     */
    @Test
    public void basic() {
        assertEquals(5, MessageState.values().length);
        
        assertEquals(MessageState.ACK_KO, MessageState.valueOf("ACK_KO"));
        assertEquals(MessageState.ACK_OK, MessageState.valueOf("ACK_OK"));
        assertEquals(MessageState.ACK_WARN, MessageState.valueOf("ACK_WARN"));
        assertEquals(MessageState.READ, MessageState.valueOf("READ"));
        assertEquals(MessageState.SEND, MessageState.valueOf("SEND"));
    }
}
