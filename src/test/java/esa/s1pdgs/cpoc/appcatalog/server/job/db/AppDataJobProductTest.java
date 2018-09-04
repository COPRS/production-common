package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobProductTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppDataJobProduct obj = new AppDataJobProduct();

        // check default constructor
        assertEquals(0, obj.getRaws1().size());
        assertEquals(0, obj.getRaws2().size());
        assertNull(obj.getStartTime());
        assertNull(obj.getStopTime());

        obj.setSessionId("session-id");
        obj.setProductName("product-name");
        obj.setSatelliteId("B");
        obj.setMissionId("S1");
        obj.setAcquisition("EW");
        obj.setStartTime(new Date());
        obj.setStopTime(new Date());
        obj.setRaws1(Arrays.asList(new AppDataJobFile("message1"),
                new AppDataJobFile("message2")));
        obj.setRaws2(Arrays.asList(new AppDataJobFile("gen1"),
                new AppDataJobFile("gen2"), new AppDataJobFile("gen3")));

        // check setters
        assertNotNull(obj.getStartTime());
        assertNotNull(obj.getStopTime());
        assertEquals("session-id", obj.getSessionId());
        assertEquals("product-name", obj.getProductName());
        assertEquals("B", obj.getSatelliteId());
        assertEquals("S1", obj.getMissionId());
        assertEquals("EW", obj.getAcquisition());

        // check toString
        String str = obj.toString();
        assertTrue(str.contains("sessionId: session-id"));
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.contains("satelliteId: B"));
        assertTrue(str.contains("missionId: S1"));
        assertTrue(str.contains("startTime: "));
        assertTrue(str.contains("stopTime: "));
        assertTrue(str.contains("acquisition: EW"));
        assertTrue(
                str.contains(
                        "raws1: " + Arrays
                                .asList(new AppDataJobFile("message1"),
                                        new AppDataJobFile("message2"))
                                .toString()));
        assertTrue(str
                .contains("raws2: " + Arrays.asList(new AppDataJobFile("gen1"),
                        new AppDataJobFile("gen2"), new AppDataJobFile("gen3"))
                        .toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobProduct.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
