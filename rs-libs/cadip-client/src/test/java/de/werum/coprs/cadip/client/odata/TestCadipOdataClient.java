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
