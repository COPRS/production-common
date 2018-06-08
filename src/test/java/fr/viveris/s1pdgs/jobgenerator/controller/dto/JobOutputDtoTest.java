package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test class JobOutputDto
 * @author Cyrielle Gailliard
 *
 */
public class JobOutputDtoTest {
	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		JobOutputDto dto = new JobOutputDto("family1", "regexp1");
		assertTrue("family1".equals(dto.getFamily()));
		assertTrue("regexp1".equals(dto.getRegexp()));
	}
	
	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		JobOutputDto dto = new JobOutputDto();
		dto.setFamily("family2");
		dto.setRegexp("regexp2");
		String str = dto.toString();
		assertTrue(str.contains("family: family2"));
		assertTrue(str.contains("regexp: regexp2"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(JobOutputDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
