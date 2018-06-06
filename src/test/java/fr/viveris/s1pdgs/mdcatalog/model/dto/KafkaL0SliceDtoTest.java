package fr.viveris.s1pdgs.mdcatalog.model.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class KafkaL0SliceDtoTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		KafkaL0SliceDto obj = new KafkaL0SliceDto("name", "kobs");
		obj = new KafkaL0SliceDto();
		obj.setProductName("name");
		obj.setKeyObjectStorage("kobs");
		
		String str = obj.toString();
		assertTrue(str.contains("productName= name"));
		assertTrue(str.contains("keyObjectStorage= kobs"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(KafkaL0SliceDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
