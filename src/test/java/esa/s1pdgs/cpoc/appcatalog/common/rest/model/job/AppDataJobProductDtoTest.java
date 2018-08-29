package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobProductDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppDataJobProductDto obj = new AppDataJobProductDto();

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
        obj.setRaws1(Arrays.asList("message1", "message2"));
        obj.setRaws2(Arrays.asList("gen1", "gen2", "gen3"));

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
        assertTrue(str.contains(
                "raws1: " + Arrays.asList("message1", "message2").toString()));
        assertTrue(str.contains(
                "raws2: " + Arrays.asList("gen1", "gen2", "gen3").toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobProductDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
