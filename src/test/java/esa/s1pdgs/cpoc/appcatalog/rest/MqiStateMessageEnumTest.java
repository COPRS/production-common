package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the enumeration MqiStateMessageEnum
 * 
 * @author Viveris Technologies
 */
public class MqiStateMessageEnumTest {

    /**
     * Test values and valueOf
     */
    @Test
    public void basic() {
        assertEquals(5, MqiStateMessageEnum.values().length);
        
        assertEquals(MqiStateMessageEnum.ACK_KO, MqiStateMessageEnum.valueOf("ACK_KO"));
        assertEquals(MqiStateMessageEnum.ACK_OK, MqiStateMessageEnum.valueOf("ACK_OK"));
        assertEquals(MqiStateMessageEnum.ACK_WARN, MqiStateMessageEnum.valueOf("ACK_WARN"));
        assertEquals(MqiStateMessageEnum.READ, MqiStateMessageEnum.valueOf("READ"));
        assertEquals(MqiStateMessageEnum.SEND, MqiStateMessageEnum.valueOf("SEND"));
    }
}
