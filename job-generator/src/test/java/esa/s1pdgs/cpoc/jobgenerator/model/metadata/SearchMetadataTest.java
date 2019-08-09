package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class SearchMetadataTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		SearchMetadata obj = new SearchMetadata("name", "type", "kobs", "start", "stop", "mission", "satellite", "station");
		
		String str = obj.toString();
		assertTrue(str.contains("productName\":\"name"));
		assertTrue(str.contains("productType\":\"type"));
		assertTrue(str.contains("keyObjectStorage\":\"kobs"));
		assertTrue(str.contains("validityStart\":\"start"));
		assertTrue(str.contains("validityStop\":\"stop"));
		assertTrue(str.contains("missionId\":\"mission"));
		assertTrue(str.contains("satelliteId\":\"satellite"));
		assertTrue(str.contains("stationId\":\"station"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SearchMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
