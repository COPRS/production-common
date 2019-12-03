package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class TestBlacklistRegexNameInboxFilter {
	private final BlacklistRegexNameInboxFilter uut = new BlacklistRegexNameInboxFilter(Pattern.compile(".+\\.tmp"));

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse() {
		assertEquals(false, uut.accept(new InboxEntry("foo.tmp", "foo.tmp", "file:///tmp")));
	}

	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue() {
		assertEquals(true, uut.accept(new InboxEntry("fooxml", "fooxml", "file:///tmp")));
	}
}
