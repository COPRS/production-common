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

package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class TestRoutingTable {

	@Test
	public void testNoRulesFound_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Collections.emptyMap());
		});
		assertEquals("No routing rules found", e.getMessage());
	}
	
}
