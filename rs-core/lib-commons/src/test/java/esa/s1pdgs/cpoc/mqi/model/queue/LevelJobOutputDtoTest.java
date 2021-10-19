package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test class JobOutputDto
 * @author Cyrielle Gailliard
 *
 */
public class LevelJobOutputDtoTest {
	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		LevelJobOutputDto dto = new LevelJobOutputDto("family1", "regexp1");
		assertTrue("family1".equals(dto.getFamily()));
		assertTrue("regexp1".equals(dto.getRegexp()));
	}
	
	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		LevelJobOutputDto dto = new LevelJobOutputDto();
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
		EqualsVerifier.forClass(LevelJobOutputDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
