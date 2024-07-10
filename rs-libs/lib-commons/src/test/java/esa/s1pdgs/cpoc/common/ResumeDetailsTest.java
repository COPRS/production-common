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

package esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ResumeDetails;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object ResumeDetails
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ResumeDetailsTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		ResumeDetails obj = new ResumeDetails("topic-name", "dto-object");
		assertEquals("topic-name", obj.getTopicName());
		assertEquals("dto-object", obj.getDto());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		ResumeDetails obj = new ResumeDetails("topic-name", "dto-object");
		String str = obj.toString();
		assertTrue(str.contains("topicName: topic-name"));
		assertTrue(str.contains("dto: dto-object"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(ResumeDetails.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
