package fr.viveris.s1pdgs.mdcatalog.model.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class KafkaL0AcnDtoTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		KafkaL0AcnDto obj = new KafkaL0AcnDto("name", "kobs");
		obj = new KafkaL0AcnDto();
		obj.setProductName("name");
		obj.setKeyObjectStorage("kobs");
		
		String str = obj.toString();
		assertTrue(str.contains("productName: name"));
		assertTrue(str.contains("keyObjectStorage: kobs"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(KafkaL0AcnDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
