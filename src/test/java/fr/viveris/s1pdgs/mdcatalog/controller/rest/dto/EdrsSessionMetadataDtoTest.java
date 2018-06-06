package fr.viveris.s1pdgs.mdcatalog.controller.rest.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto.EdrsSessionMetadataDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class EdrsSessionMetadataDtoTest {

	/**
	 * Test toString
	 */
	//String productName, String productType, String keyObjectStorage, String validityStart,String validityStop
	@Test
	public void testToString() {
		EdrsSessionMetadataDto obj = new EdrsSessionMetadataDto("name", "type", "kobs", "startDate", "stopDate");
		obj = new EdrsSessionMetadataDto(obj);
		
		String str = obj.toString();
		assertTrue(str.contains("productName=name"));
		assertTrue(str.contains("productType=type"));
		assertTrue(str.contains("keyObjectStorage=kobs"));
		assertTrue(str.contains("validityStart=startDate"));
		assertTrue(str.contains("validityStop=stopDate"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(EdrsSessionMetadataDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
