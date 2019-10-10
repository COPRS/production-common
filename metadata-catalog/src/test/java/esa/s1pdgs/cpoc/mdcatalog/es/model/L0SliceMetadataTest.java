package esa.s1pdgs.cpoc.mdcatalog.es.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class L0SliceMetadataTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		L0SliceMetadata obj = new L0SliceMetadata();
		obj.setProductName("name");
		obj.setProductType("type");
		obj.setKeyObjectStorage("kobs");
		obj.setValidityStart("start");
		obj.setValidityStop("stop");
		obj.setInstrumentConfigurationId(15);
		obj.setNumberSlice(4);
		obj.setDatatakeId("14256");
		
		String str = obj.toJsonString();
		assertTrue(str.contains("productName\":\"name"));
		assertTrue(str.contains("productType\":\"type"));
		assertTrue(str.contains("keyObjectStorage\":\"kobs"));
		assertTrue(str.contains("validityStart\":\"start"));
		assertTrue(str.contains("validityStop\":\"stop"));
		assertTrue(str.contains("instrumentConfigurationId\":15"));
		assertTrue(str.contains("numberSlice\":4"));
		assertTrue(str.contains("datatakeId\":\"14256"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(L0SliceMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
