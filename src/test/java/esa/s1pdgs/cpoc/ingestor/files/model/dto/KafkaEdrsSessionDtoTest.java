package esa.s1pdgs.cpoc.ingestor.files.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestor.files.model.EdrsSessionFileType;
import esa.s1pdgs.cpoc.ingestor.files.model.dto.KafkaEdrsSessionDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Cyrielle Gailliard
 *
 */
public class KafkaEdrsSessionDtoTest {

	/**
	 * Test getters, setters and constructors
	 */
	@Test
	public void testGettersSettersConstructors() {
		KafkaEdrsSessionDto dto = new KafkaEdrsSessionDto("key-obs", 2, EdrsSessionFileType.RAW, "S1", "B");
		assertEquals("key-obs", dto.getObjectStorageKey());
		assertEquals(2, dto.getChannelId());
		assertEquals(EdrsSessionFileType.RAW, dto.getProductType());
		assertEquals("S1", dto.getMissionId());
		assertEquals("B", dto.getSatelliteId());

		dto.setObjectStorageKey("other-key");
		dto.setChannelId(15);
		dto.setMissionId("other-mission");
		dto.setSatelliteId("other-sat");
		dto.setProductType(EdrsSessionFileType.SESSION);
		assertEquals("other-key", dto.getObjectStorageKey());
		assertEquals(15, dto.getChannelId());
		assertEquals(EdrsSessionFileType.SESSION, dto.getProductType());
		assertEquals("other-mission", dto.getMissionId());
		assertEquals("other-sat", dto.getSatelliteId());
	}

	/**
	 * Test the toString function
	 */
	@Test
	public void testToString() {
		KafkaEdrsSessionDto dto = new KafkaEdrsSessionDto("key-obs", 2, EdrsSessionFileType.RAW, "S1", "B");
		String str = dto.toString();
		assertTrue("toString should contain the key OBS", str.contains("objectStorageKey: key-obs"));
		assertTrue("toString should contain the channel id", str.contains("channelId: 2"));
		assertTrue("toString should contain the product type", str.contains("productType: RAW"));
		assertTrue("toString should contain the mission id", str.contains("missionId: S1"));
		assertTrue("toString should contain the satellite id", str.contains("satelliteId: B"));
	}

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(KafkaEdrsSessionDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
