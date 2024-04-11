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

package de.werum.coprs.cadip.client.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.Test;

public class TestCadipOdataClient {

	@Test
	public final void testToNormalizedUri_OnTrailingSlash_ShallUseOriginalUri() {
		final String uriString = "http://localhost/odata/v1/";
		assertEquals(uriString, CadipOdataClient.toNormalizedUri(uriString).toString());
	}

	@Test
	public final void testToNormalizedUri_OnMissingTrailingSlash_ShallAppendSlash() {
		final String uriString = "http://localhost/odata/v1";
		assertEquals(uriString + "/", CadipOdataClient.toNormalizedUri(uriString).toString());
	}
	
	@Test
	public final void testUriResolution() {
		final String uriString = "http://localhost/odata/v1";
		final URI uri = CadipOdataClient.toNormalizedUri(uriString);
		
		assertEquals("http://localhost/odata/v1/foo", uri.resolve("foo").toString());
		
		// undesired behavior as 'v1' is missing
		assertEquals("http://localhost/odata/foo", URI.create(uriString).resolve("foo").toString());
	}
}
