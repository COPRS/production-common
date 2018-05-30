package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class JobTaskDto
 * @author Cyrielle
 *
 */
public class JobTaskDtoTest {

	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		JobTaskDto dto = new JobTaskDto("path1");
		assertTrue("path1".equals(dto.getBinaryPath()));
	}
	
	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		JobTaskDto dto = new JobTaskDto();
		dto.setBinaryPath("path1");
		String str = dto.toString();
		assertTrue(str.contains("binaryPath: path1"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(JobTaskDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
