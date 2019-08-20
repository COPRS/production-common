package esa.s1pdgs.cpoc.mdcatalog.es.model;

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
		SearchMetadata obj = new SearchMetadata();
		obj.setProductName("name");
		obj.setProductType("type");
		obj.setKeyObjectStorage("kobs");
		obj.setValidityStart("start");
		obj.setValidityStop("stop");
		String str = obj.toJsonString();
		assertTrue(str.contains("\"productName\":\"name\""));
		assertTrue(str.contains("\"productType\":\"type\""));
		assertTrue(str.contains("\"keyObjectStorage\":\"kobs\""));
		assertTrue(str.contains("\"validityStart\":\"start\""));
		assertTrue(str.contains("\"validityStop\":\"stop\""));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SearchMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
