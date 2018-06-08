package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.jobgenerator.controller.dto.EdrsSessionDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaEdrsSessionDto
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionDtoTest {

	/**
	 * Test the "equals" definition
	 */
	@Test
	public void testEqualsFunction() {
		EdrsSessionDto dto1 = new EdrsSessionDto("testEqualsFunction", 1, "RAW", "S1", "A");
		EdrsSessionDto dto2 = new EdrsSessionDto("testEqualsFunction", 2, "RAW", "S1", "A");
		EdrsSessionDto dto3 = new EdrsSessionDto("testEqualsFunction2", 2, "RAW", "S1", "A");
		EdrsSessionDto dto4 = new EdrsSessionDto("testEqualsFunction2", 2, "SESSION", "S1", "A");
		EdrsSessionDto dto5 = new EdrsSessionDto("testEqualsFunction2", 2, "RAW", "S1", "A");
		assertFalse(String.format("%s shall equal %s", dto1, dto2), dto1.equals(dto2));
		assertFalse(String.format("%s shall equal %s", dto1, dto3), dto1.equals(dto3));
		assertFalse(String.format("%s shall equal %s", dto2, dto3), dto2.equals(dto3));
		assertFalse(String.format("%s shall equal %s", dto3, dto4), dto3.equals(dto4));
		assertTrue(String.format("%s shall equal %s", dto3, dto5), dto3.equals(dto5));
	}

	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		EdrsSessionDto dto1 = new EdrsSessionDto("testEqualsFunction", 1, "RAW", "S1", "A");
		assertTrue("testEqualsFunction".equals(dto1.getObjectStorageKey()));
		assertTrue("RAW".equals(dto1.getProductType()));
		assertTrue(dto1.getChannelId() == 1);
		assertTrue("S1".equals(dto1.getMissionId()));
		assertTrue("A".equals(dto1.getSatelliteId()));
	}

	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		EdrsSessionDto dto1 = new EdrsSessionDto();
		dto1.setObjectStorageKey("testEqualsFunction");
		dto1.setChannelId(2);
		dto1.setMissionId("S1");
		dto1.setProductType("RAW");
		dto1.setSatelliteId("A");
		String str = dto1.toString();
		assertTrue(str.contains("objectStorageKey: "));
		assertTrue(str.contains("channelId: 2"));
		assertTrue(str.contains("missionId: S1"));
		assertTrue(str.contains("productType: RAW"));
		assertTrue(str.contains("satelliteId: A"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(EdrsSessionDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
