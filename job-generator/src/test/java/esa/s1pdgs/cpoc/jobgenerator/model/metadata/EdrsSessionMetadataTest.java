package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.jobgenerator.model.metadata.EdrsSessionMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class EdrsSessionMetadataTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		EdrsSessionMetadata obj = new EdrsSessionMetadata("", "", "", "", "");
		obj.setProductName("name");
		obj.setProductType("type");
		obj.setKeyObjectStorage("kobs");
		obj.setValidityStart("start");
		obj.setValidityStop("stop");
		
		String str = obj.toString();
		assertTrue(str.contains("productName: name"));
		assertTrue(str.contains("productType: type"));
		assertTrue(str.contains("keyObjectStorage: kobs"));
		assertTrue(str.contains("validityStart: start"));
		assertTrue(str.contains("validityStop: stop"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(EdrsSessionMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
