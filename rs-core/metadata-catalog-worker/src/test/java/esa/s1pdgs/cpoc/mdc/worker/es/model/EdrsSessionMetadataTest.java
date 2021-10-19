package esa.s1pdgs.cpoc.mdc.worker.es.model;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
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
		EdrsSessionMetadata obj = new EdrsSessionMetadata();
		obj.setProductName("name");
		obj.setProductType("type");
		obj.setKeyObjectStorage("kobs");
		obj.setStartTime("start");
		obj.setStopTime("stop");
		obj.setValidityStart("vstart");
		obj.setValidityStop("vstop");
		obj.setRawNames(Arrays.<String>asList("a","b","c"));
		
		String str = obj.toJsonString();
		assertTrue(str.contains("productName\":\"name"));
		assertTrue(str.contains("productType\":\"type"));
		assertTrue(str.contains("keyObjectStorage\":\"kobs"));
		assertTrue(str.contains("startTime\":\"start"));
		assertTrue(str.contains("stopTime\":\"stop"));
		assertTrue(str.contains("validityStart\":\"vstart"));
		assertTrue(str.contains("validityStop\":\"vstop"));
		assertTrue(str.contains("rawNames\":[\"a\",\"b\",\"c\"]"));
	}

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(EdrsSessionMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
