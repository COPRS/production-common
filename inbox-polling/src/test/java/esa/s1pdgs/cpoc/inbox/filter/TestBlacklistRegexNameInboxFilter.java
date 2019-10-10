package esa.s1pdgs.cpoc.inbox.filter;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

public class TestBlacklistRegexNameInboxFilter {
	private final BlacklistRegexNameInboxFilter uut = new BlacklistRegexNameInboxFilter(Pattern.compile(".+\\.tmp"));

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse() {
		assertEquals(false,
				uut.accept(new InboxEntry("foo.tmp", "foo.tmp", "file:///tmp/MPS_/S1A/foo.tmp", "S1", "A", "MPS_")));
	}

	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue() {
		assertEquals(true,
				uut.accept(new InboxEntry("fooxml", "fooxml", "file:///tmp/MPS_/S1A/fooxml", "S1", "A", "MPS_")));
	}
}
