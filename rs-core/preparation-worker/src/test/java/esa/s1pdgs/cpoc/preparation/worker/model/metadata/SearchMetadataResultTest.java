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

package esa.s1pdgs.cpoc.preparation.worker.model.metadata;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class SearchMetadataResultTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToJsonString() {
		SearchMetadataQuery query = new SearchMetadataQuery(12, "retrievalMode", 0.0, 1.5, "productType", ProductFamily.L0_SLICE);
		SearchMetadataQuery query2 = new SearchMetadataQuery(1, "retrievalode", 2.1, 1.3, "productype", ProductFamily.L0_SLICE);
		SearchMetadata result = new SearchMetadata("name", "type", "kobs", "start", "stop", "mission", "satellite", "station");

		SearchMetadataResult obj = new SearchMetadataResult(query);
		obj.setResult(Arrays.asList(result));

		String str = obj.toJsonString();
		assertTrue(str.contains("query: "));
		assertTrue(str.contains("result: "));

		obj.setQuery(query2);

		str = obj.toJsonString();
		assertTrue(str.contains("query: "));
		assertTrue(str.contains("result: "));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SearchMetadataResult.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
