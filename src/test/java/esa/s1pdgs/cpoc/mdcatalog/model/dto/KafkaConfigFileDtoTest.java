package esa.s1pdgs.cpoc.mdcatalog.model.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdcatalog.model.dto.KafkaConfigFileDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class KafkaConfigFileDtoTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		KafkaConfigFileDto obj = new KafkaConfigFileDto("name", "kobs");
		obj = new KafkaConfigFileDto();
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
		EqualsVerifier.forClass(KafkaConfigFileDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
