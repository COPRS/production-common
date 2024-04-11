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

package esa.s1pdgs.cpoc.preparation.worker.type.pdu.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.time.TimeInterval;

public class PDUGeneratorTest {

	@Test
	public void generateTimeIntervalsShouldMergeLastTImeInterval() {
		PDUFrameGenerator generator = new PDUFrameGenerator(null, null, null);

		List<TimeInterval> intervals = generator.generateTimeIntervals("2023-07-20T10:00:00.000000Z",
				"2023-07-20T11:00:00.500000Z", 60, 1);
		
		assertEquals(intervals.size(), 60);
	}
}
