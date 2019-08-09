package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class EdrsSessionMetadataDtoTest {

	/**
	 * Test toString
	 */
	//String productName, String productType, String keyObjectStorage, String validityStart,String validityStop
	@Test
	public void testToString() {
		EdrsSessionMetadata obj = new EdrsSessionMetadata("name", "type", "kobs", "startDate", "stopDate", "mission", "satellite", "station");
		
		String str = obj.toString();
		assertTrue(str.contains("\"productName\":\"name\""));
		assertTrue(str.contains("\"productType\":\"type\""));
		assertTrue(str.contains("\"keyObjectStorage\":\"kobs\""));
		assertTrue(str.contains("\"validityStart\":\"startDate\""));
		assertTrue(str.contains("\"validityStop\":\"stopDate\""));
		assertTrue(str.contains("\"missionId\":\"mission\""));
		assertTrue(str.contains("\"satelliteId\":\"satellite\""));
		assertTrue(str.contains("\"stationCode\":\"station\""));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(EdrsSessionMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
