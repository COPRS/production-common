package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.jobgenerator.controller.dto.EdrsSessionDto;

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
	public void testConstructors() {
		EdrsSessionDto dto1 = new EdrsSessionDto("testEqualsFunction", 1, "RAW", "S1", "A");
		assertTrue("testEqualsFunction".equals(dto1.getObjectStorageKey()));
		assertTrue("RAW".equals(dto1.getProductType()));
		assertTrue(dto1.getChannelId() == 1);
		assertTrue("S1".equals(dto1.getMissionId()));
		assertTrue("A".equals(dto1.getSatelliteId()));
	}
}
