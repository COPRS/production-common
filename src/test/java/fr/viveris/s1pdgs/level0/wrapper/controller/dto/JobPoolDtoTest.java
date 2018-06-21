package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class jobPoolDtoTest
 * 
 * @author Cyrielle Gailliard
 *
 */
public class JobPoolDtoTest {
	private JobTaskDto task1;
	private JobTaskDto task2;
	private JobTaskDto task3;

	@Before
	public void init() {
		task1 = new JobTaskDto("path1");
		task2 = new JobTaskDto("path2");
		task3 = new JobTaskDto("path3");
	}
	
	private void checkDto(JobPoolDto dto) {
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
		JobPoolDto dto = new JobPoolDto();
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
		JobPoolDto dto = new JobPoolDto();
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
		EqualsVerifier.forClass(JobPoolDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
