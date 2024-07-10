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
