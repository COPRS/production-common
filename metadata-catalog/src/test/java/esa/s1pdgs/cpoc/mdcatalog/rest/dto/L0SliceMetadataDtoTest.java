package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdcatalog.rest.dto.L0SliceMetadataDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class L0SliceMetadataDtoTest {

	/**
	 * Test toString
	 */
	//String productName, String productType, String keyObjectStorage, String validityStart,String validityStop
	@Test
	public void testToString() {
		L0SliceMetadataDto obj = new L0SliceMetadataDto("name", "type", "kobs", "startDate", "stopDate");
		obj = new L0SliceMetadataDto(obj);
		obj.setInstrumentConfigurationId(1);
		obj.setDatatakeId("dataTakeId");
		obj.setNumberSlice(8);
		
		String str = obj.toString();
		assertTrue(str.contains("\"productName\":\"name\""));
		assertTrue(str.contains("\"productType\":\"type\""));
		assertTrue(str.contains("\"keyObjectStorage\":\"kobs\""));
		assertTrue(str.contains("\"validityStart\":\"startDate\""));
		assertTrue(str.contains("\"validityStop\":\"stopDate\""));
		assertTrue(str.contains("\"instrumentConfigurationId\":1"));
		assertTrue(str.contains("\"datatakeId\":\"dataTakeId\""));
		assertTrue(str.contains("\"numberSlice\":8"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(L0SliceMetadataDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
