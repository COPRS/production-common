package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class L0AcnMetadataTest {

	/**
	 * Test toString
	 */
	//String productName, String productType, String keyObjectStorage, String validityStart,String validityStop
	@Test
	public void testToString() {
		L0AcnMetadata obj = new L0AcnMetadata("name", "type", "kobs", "startDate", "stopDate", "mission", "satellite", "station");
		obj.setInstrumentConfigurationId(1);
		obj.setDatatakeId("dataTakeId");
		obj.setNumberOfSlices(8);
		
		String str = obj.toString();
		assertTrue(str.contains("\"productName\":\"name\""));
		assertTrue(str.contains("\"productType\":\"type\""));
		assertTrue(str.contains("\"keyObjectStorage\":\"kobs\""));
		assertTrue(str.contains("\"validityStart\":\"startDate\""));
		assertTrue(str.contains("\"validityStop\":\"stopDate\""));
		assertTrue(str.contains("\"missionId\":\"mission\""));
		assertTrue(str.contains("\"satelliteId\":\"satellite\""));
		assertTrue(str.contains("\"stationId\":\"station\""));
		assertTrue(str.contains("\"instrumentConfigurationId\":1"));
		assertTrue(str.contains("\"datatakeId\":\"dataTakeId\""));
		assertTrue(str.contains("\"numberOfSlices\":8"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(L0AcnMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
