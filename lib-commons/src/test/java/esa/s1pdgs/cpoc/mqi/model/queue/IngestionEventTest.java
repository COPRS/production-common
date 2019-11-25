package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Viveris Technologies
 *
 */
public class IngestionEventTest {

	/**
	 * Test getters, setters and constructors
	 */
	@Test
	public void testGettersSettersConstructors() {
		IngestionEvent dto = new IngestionEvent("key-obs", "/path/of/inbox", 2, EdrsSessionFileType.RAW, "S1", "B", "WILE", "sessionId");
		assertEquals("key-obs", dto.getKeyObjectStorage());
		assertEquals("/path/of/inbox", dto.getInboxPath());
		assertEquals(2, dto.getChannelId());
		assertEquals(EdrsSessionFileType.RAW, dto.getProductType());
		assertEquals("S1", dto.getMissionId());
		assertEquals("B", dto.getSatelliteId());

		dto = new IngestionEvent();
		dto.setKeyObjectStorage("other-key");
		dto.setChannelId(15);
		dto.setMissionId("other-mission");
		dto.setSatelliteId("other-sat");
		dto.setProductType(EdrsSessionFileType.SESSION);
		assertEquals("other-key", dto.getKeyObjectStorage());
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
		final IngestionEvent dto = new IngestionEvent("key-obs", "/path/of/inbox", 2, EdrsSessionFileType.RAW, "S1", "B", "WILE", "sessionId");
		final String str = dto.toString();
		assertTrue("toString should contain the key OBS", str.contains("key-obs"));
		assertTrue("toString should contain the inbox path", str.contains("/path/of/inbox"));
		assertTrue("toString should contain the channel id", str.contains("2"));
		assertTrue("toString should contain the product type", str.contains("RAW"));
		assertTrue("toString should contain the mission id", str.contains("S1"));
		assertTrue("toString should contain the satellite id", str.contains("B"));
	}

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(IngestionEvent.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
