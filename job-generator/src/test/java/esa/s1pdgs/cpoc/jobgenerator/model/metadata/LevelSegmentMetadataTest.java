package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class LevelSegmentMetadataTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		LevelSegmentMetadata obj = new LevelSegmentMetadata("name", "type", "kobs", "start", "stop","pol","consol","14256");
		assertEquals("name", obj.getProductName());
        assertEquals("type", obj.getProductType());
        assertEquals("kobs", obj.getKeyObjectStorage());
        assertEquals("start", obj.getValidityStart());
        assertEquals("stop", obj.getValidityStop());
        assertEquals("14256", obj.getDatatakeId());
        assertEquals("consol", obj.getConsolidation());
        assertEquals("pol", obj.getPolarisation());
		
		String str = obj.toString();
		assertTrue(str.contains("productName: name"));
		assertTrue(str.contains("productType: type"));
		assertTrue(str.contains("keyObjectStorage: "));
		assertTrue(str.contains("validityStart: start"));
		assertTrue(str.contains("validityStop: stop"));
		assertTrue(str.contains("consolidation: consol"));
		assertTrue(str.contains("polarisation: pol"));
		assertTrue(str.contains("datatakeId: 14256"));
		
		obj.setProductName("name2");
		obj.setProductType("type2");
		obj.setKeyObjectStorage("kobs2");
		obj.setValidityStart("start2");
		obj.setValidityStop("stop2");
        obj.setDatatakeId("142562");
        obj.setConsolidation("consol2");
        obj.setPolarisation("pol2");
        assertEquals("name2", obj.getProductName());
        assertEquals("type2", obj.getProductType());
        assertEquals("kobs2", obj.getKeyObjectStorage());
        assertEquals("start2", obj.getValidityStart());
        assertEquals("stop2", obj.getValidityStop());
        assertEquals("142562", obj.getDatatakeId());
        assertEquals("consol2", obj.getConsolidation());
        assertEquals("pol2", obj.getPolarisation());
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(LevelSegmentMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
