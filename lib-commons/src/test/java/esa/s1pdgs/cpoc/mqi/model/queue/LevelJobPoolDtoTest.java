package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class jobPoolDtoTest
 * 
 * @author Cyrielle Gailliard
 *
 */
public class LevelJobPoolDtoTest {
	private LevelJobTaskDto task1;
	private LevelJobTaskDto task2;
	private LevelJobTaskDto task3;

	@Before
	public void init() {
		task1 = new LevelJobTaskDto("path1");
		task2 = new LevelJobTaskDto("path2");
		task3 = new LevelJobTaskDto("path3");
	}
	
	private void checkDto(LevelJobPoolDto dto) {
		assertTrue(3 == dto.getTasks().size());
		assertEquals(dto.getTasks().get(0), task1);
		assertEquals(dto.getTasks().get(1), task2);
		assertEquals(dto.getTasks().get(2), task3);
	}

	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		LevelJobPoolDto dto = new LevelJobPoolDto();
		dto.addTask(task1);
		dto.addTask(task2);
		dto.addTask(task3);
		checkDto(dto);		
	}

	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		LevelJobPoolDto dto = new LevelJobPoolDto();
		dto.setTasks(Arrays.asList(task1, task2, task3));
		checkDto(dto);		

		String str = dto.toString();
		assertTrue(str.contains("tasks:"));
		assertTrue(str.contains(task1.toString()));
		assertTrue(str.contains(task2.toString()));
		assertTrue(str.contains(task3.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(LevelJobPoolDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
