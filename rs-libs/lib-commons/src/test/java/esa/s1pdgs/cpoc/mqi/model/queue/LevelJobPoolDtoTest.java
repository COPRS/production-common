/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
