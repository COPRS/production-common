package esa.s1pdgs.cpoc.inbox.polling.filter;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.inbox.polling.filter.BlacklistRegexNameInboxFilter;

public class TestBlacklistRegexNameInboxFilter {
	private final BlacklistRegexNameInboxFilter uut = new BlacklistRegexNameInboxFilter(Pattern.compile(".+\\.tmp"));

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse() {
		assertEquals(false, uut.accept(new FakeInboxEntry("foo.tmp")));
	}
	
	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue() {
		assertEquals(true, uut.accept(new FakeInboxEntry("fooxml")));
	}
}
