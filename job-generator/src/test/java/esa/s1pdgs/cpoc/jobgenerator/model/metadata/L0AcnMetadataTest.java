package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class L0AcnMetadataTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToJsonString() {
		L0AcnMetadata obj = new L0AcnMetadata("", "", "", "", "", "", "", "", 5, 1, "123456");
		obj.setProductName("name");
		obj.setProductType("type");
		obj.setKeyObjectStorage("kobs");
		obj.setValidityStart("start");
		obj.setValidityStop("stop");
		obj.setInstrumentConfigurationId(15);
		obj.setNumberOfSlices(11);
		obj.setDatatakeId("14256");
		
		String str = obj.toJsonString();
		assertTrue(str.contains("productName\":\"name"));
		assertTrue(str.contains("productType\":\"type"));
		assertTrue(str.contains("keyObjectStorage\":\"kobs"));
		assertTrue(str.contains("validityStart\":\"start"));
		assertTrue(str.contains("validityStop\":\"stop"));
		assertTrue(str.contains("instrumentConfigurationId\":15"));
		assertTrue(str.contains("numberOfSlices\":11"));
		assertTrue(str.contains("datatakeId\":\"14256"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(L0AcnMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
