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
        obj.setRaws1(Arrays.asList(new AppDataJobFileDto("message1"),
                new AppDataJobFileDto("message2")));
        obj.setRaws2(Arrays.asList(new AppDataJobFileDto("gen1"),
                new AppDataJobFileDto("gen2"), new AppDataJobFileDto("gen3")));
        obj.setDataTakeId("datatake-id");
        obj.setInsConfId(4);
        obj.setNumberSlice(125);
        obj.setTotalNbOfSlice(1425);
        obj.setProductType("product-type");
        obj.setSegmentStartDate("2018-12-12T21:25:12");
        obj.setSegmentStopDate("2018-12-12T21:28:12");

        // check setters
        assertNotNull(obj.getStartTime());
        assertNotNull(obj.getStopTime());
        assertEquals("session-id", obj.getSessionId());
        assertEquals("product-name", obj.getProductName());
        assertEquals("B", obj.getSatelliteId());
        assertEquals("S1", obj.getMissionId());
        assertEquals("EW", obj.getAcquisition());
        assertEquals("datatake-id", obj.getDataTakeId());
        assertEquals(4, obj.getInsConfId());
        assertEquals(125, obj.getNumberSlice());
        assertEquals(1425, obj.getTotalNbOfSlice());
        assertEquals("product-type", obj.getProductType());
        assertEquals("2018-12-12T21:25:12", obj.getSegmentStartDate());
        assertEquals("2018-12-12T21:28:12", obj.getSegmentStopDate());

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
                "raws1: " + Arrays.asList(new AppDataJobFileDto("message1"),
                        new AppDataJobFileDto("message2"))));
        assertTrue(str.contains("raws2: " + Arrays.asList(
                new AppDataJobFileDto("gen1"), new AppDataJobFileDto("gen2"),
                new AppDataJobFileDto("gen3"))));
        assertTrue(str.contains("dataTakeId: datatake-id"));
        assertTrue(str.contains("insConfId: 4"));
        assertTrue(str.contains("numberSlice: 125"));
        assertTrue(str.contains("totalNbOfSlice: 1425"));
        assertTrue(str.contains("productType: product-type"));
        assertTrue(str.contains("segmentStartDate: 2018-12-12T21:25:12"));
        assertTrue(str.contains("segmentStopDate: 2018-12-12T21:28:12"));
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
