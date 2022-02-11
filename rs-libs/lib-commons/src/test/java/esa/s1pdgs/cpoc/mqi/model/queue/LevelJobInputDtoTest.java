package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test class JoInputDto
 * @author Cyrielle Gailliard
 *
 */
public class LevelJobInputDtoTest {
	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		LevelJobInputDto dto = new LevelJobInputDto("fam", "local-path", "content-ref");
		assertTrue("fam".equals(dto.getFamily()));
		assertTrue("local-path".equals(dto.getLocalPath()));
		assertTrue("content-ref".equals(dto.getContentRef()));
	}
	
	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		LevelJobInputDto dto = new LevelJobInputDto();
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
		EqualsVerifier.forClass(LevelJobInputDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
