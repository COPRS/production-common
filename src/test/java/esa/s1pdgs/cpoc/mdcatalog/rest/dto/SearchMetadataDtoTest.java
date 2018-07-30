package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdcatalog.rest.dto.SearchMetadataDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class SearchMetadataDtoTest {

	/**
	 * Test toString
	 */
	//String productName, String productType, String keyObjectStorage, String validityStart,String validityStop
	@Test
	public void testToString() {
		SearchMetadataDto obj = new SearchMetadataDto("name", "type", "kobs", "startDate", "stopDate");
		obj = new SearchMetadataDto(obj);
		
		String str = obj.toString();
		assertTrue(str.contains("\"productName\":\"name\""));
		assertTrue(str.contains("\"productType\":\"type\""));
		assertTrue(str.contains("\"keyObjectStorage\":\"kobs\""));
		assertTrue(str.contains("\"validityStart\":\"startDate\""));
		assertTrue(str.contains("\"validityStop\":\"stopDate\""));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SearchMetadataDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
