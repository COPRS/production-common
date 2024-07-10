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

package de.werum.coprs.cadip.client.xml;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestDSIBXmlGenerator {

	@Test
	public void testDSIBGenerator() {
		List<String> filenames = new ArrayList<>();

		for (int c = 0; c <= 46; c++) {
			filenames.add("DCS_04_S1B_20200318035405020741_ch1_DSDB_" + String.format("%05d", c) + ".raw");
		}
		String result = DSIBXmlGenerator.generate("S1B_20200318035405020741", filenames, "2020-01-20T16:29:33Z",
				"2020-01-20T16:35:47Z", 13082456436L);
		System.out.println(result);
	}
}
