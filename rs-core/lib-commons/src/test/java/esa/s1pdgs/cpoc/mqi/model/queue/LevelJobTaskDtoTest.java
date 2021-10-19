package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class JobTaskDto
 * @author Cyrielle
 *
 */
public class LevelJobTaskDtoTest {

	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		LevelJobTaskDto dto = new LevelJobTaskDto("path1");
		assertTrue("path1".equals(dto.getBinaryPath()));
	}
	
	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		LevelJobTaskDto dto = new LevelJobTaskDto();
		dto.setBinaryPath("path1");
		String str = dto.toString();
		assertTrue(str.contains("binaryPath: path1"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(LevelJobTaskDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
