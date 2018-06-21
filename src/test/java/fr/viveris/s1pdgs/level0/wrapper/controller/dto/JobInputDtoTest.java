package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test class JoInputDto
 * @author Cyrielle Gailliard
 *
 */
public class JobInputDtoTest {
	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		JobInputDto dto = new JobInputDto("fam", "local-path", "content-ref");
		assertTrue("fam".equals(dto.getFamily()));
		assertTrue("local-path".equals(dto.getLocalPath()));
		assertTrue("content-ref".equals(dto.getContentRef()));
	}
	
	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		JobInputDto dto = new JobInputDto();
		dto.setFamily("family2");
		dto.setLocalPath("local-path-2");
		dto.setContentRef("content-ref-2");
		String str = dto.toString();
		assertTrue(str.contains("family: family2"));
		assertTrue(str.contains("localPath: local-path-2"));
		assertTrue(str.contains("contentRef: content-ref-2"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(JobInputDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
