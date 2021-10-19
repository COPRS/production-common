package esa.s1pdgs.cpoc.mdc.worker.rest.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class LevelSegmentMetadataDtoTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		LevelSegmentMetadata obj = new LevelSegmentMetadata("name", "type", "kobs", "start", "stop", "mission", "satellite", "station");
		assertEquals("name", obj.getProductName());
        assertEquals("type", obj.getProductType());
        assertEquals("kobs", obj.getKeyObjectStorage());
        assertEquals("start", obj.getValidityStart());
        assertEquals("stop", obj.getValidityStop());
		
		obj.setDatatakeId("14256");
		obj.setConsolidation("consol");
		obj.setPolarisation("pol");
        assertEquals("14256", obj.getDatatakeId());
        assertEquals("consol", obj.getConsolidation());
        assertEquals("pol", obj.getPolarisation());
		
		String str = obj.toJsonString();
		assertTrue(str.contains("\"productName\":\"name"));
		assertTrue(str.contains("\"productType\":\"type"));
		assertTrue(str.contains("\"keyObjectStorage\":"));
		assertTrue(str.contains("\"validityStart\":\"start"));
		assertTrue(str.contains("\"validityStop\":\"stop"));
		assertTrue(str.contains("\"missionId\":\"mission\""));
		assertTrue(str.contains("\"satelliteId\":\"satellite\""));
		assertTrue(str.contains("\"stationCode\":\"station\""));
		assertTrue(str.contains("\"consolidation\":\"consol"));
		assertTrue(str.contains("\"polarisation\":\"pol"));
		assertTrue(str.contains("\"datatakeId\":\"14256"));
		
		obj.setProductName("name2");
		obj.setProductType("type2");
		obj.setKeyObjectStorage("kobs2");
		obj.setValidityStart("start2");
		obj.setValidityStop("stop2");
        assertEquals("name2", obj.getProductName());
        assertEquals("type2", obj.getProductType());
        assertEquals("kobs2", obj.getKeyObjectStorage());
        assertEquals("start2", obj.getValidityStart());
        assertEquals("stop2", obj.getValidityStop());
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(LevelSegmentMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
