package esa.s1pdgs.cpoc.disseminator.outbox;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestUtils {	
	@Test
	public final void testAssertValidPath_OnLeadingSlash_ShallThrowException() throws Exception {
		try {
			Utils.assertValidPath("/foo/bar");
		} catch (IllegalArgumentException e) {
			// expected
			assertEquals("path must not start with '/': /foo/bar", e.getMessage());
		}
	}
	
	@Test
	public final void testAssertValidPath_IfContainsDoubleDots_ShallThrowException() throws Exception {
		try {
			Utils.assertValidPath("foo/bar/../foo");
		} catch (IllegalArgumentException e) {
			// expected
			assertEquals("path must not contain '..': foo/bar/../foo", e.getMessage());
		}
	}
}
