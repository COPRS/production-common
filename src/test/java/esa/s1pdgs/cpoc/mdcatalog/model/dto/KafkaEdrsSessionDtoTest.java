package esa.s1pdgs.cpoc.mdcatalog.model.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.mdcatalog.model.dto.KafkaEdrsSessionDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class KafkaEdrsSessionDtoTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		KafkaEdrsSessionDto obj = new KafkaEdrsSessionDto("kobs", 1, EdrsSessionFileType.RAW);
		obj = new KafkaEdrsSessionDto();
		obj.setChannelId(1);
		obj.setProductType(EdrsSessionFileType.RAW);
		obj.setObjectStorageKey("kobs");
		
		String str = obj.toString();
		assertTrue(str.contains("channelId: 1"));
		assertTrue(str.contains("objectStorageKey: kobs"));
		assertTrue(str.contains("productType: RAW"));
		
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(KafkaEdrsSessionDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
