package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class SearchMetadataDtoTest {

	/**
	 * Test toString
	 */
	//String productName, String productType, String keyObjectStorage, String validityStart,String validityStop
	@Test
	public void testToString() {
		SearchMetadata obj = new SearchMetadata("name", "type", "kobs", "startDate", "stopDate");
		
		String str = obj.toString();
        assertTrue(str.contains("productName\":\"name"));
        assertTrue(str.contains("productType\":\"type"));
        assertTrue(str.contains("keyObjectStorage\":\"kobs"));
        assertTrue(str.contains("validityStart\":\"start"));
        assertTrue(str.contains("validityStop\":\"stop"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SearchMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
