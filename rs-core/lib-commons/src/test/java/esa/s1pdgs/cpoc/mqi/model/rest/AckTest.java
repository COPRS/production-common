package esa.s1pdgs.cpoc.mqi.model.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

/**
 * Test the enumeration Ack
 * @author Viveris Technologies
 *
 */
public class AckTest {

    /**
     * Check basic functions of the enumeration
     */
    @Test
    public void testEnumFunctions() {
        assertEquals(3, Ack.values().length);
        assertEquals(Ack.OK, Ack.valueOf("OK"));
        assertEquals(Ack.WARN, Ack.valueOf("WARN"));
        assertEquals(Ack.ERROR, Ack.valueOf("ERROR"));
    }
}
