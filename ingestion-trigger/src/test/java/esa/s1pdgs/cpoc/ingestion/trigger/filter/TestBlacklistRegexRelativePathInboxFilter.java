package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.BlacklistRegexRelativePathInboxFilter;

public class TestBlacklistRegexRelativePathInboxFilter {

	private final BlacklistRegexRelativePathInboxFilter filterForSessions = new BlacklistRegexRelativePathInboxFilter(
			Pattern.compile("(^\\..*|^.*/\\..*|.*\\.tmp$|^lost\\+found$)"));

	private final BlacklistRegexRelativePathInboxFilter filterForAux = new BlacklistRegexRelativePathInboxFilter(
			Pattern.compile("(^\\..*|.+/.+|.*\\.tmp$|^lost\\+found$)"));

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse_forSession() {
		assertEquals(false, filterForSessions.accept(new InboxEntry("foo.tmp", "dir1/dir2/foo.tmp",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.tmp", "S1", "A", "MPS_")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse2_forSession() {
		assertEquals(false, filterForSessions.accept(new InboxEntry(".foo.xml", "dir1/dir2/.foo.xml",
				"file:///tmp/MPS_/S1A/dir1/dir2/.foo.xml", "S1", "A", "MPS_")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse3_forSession() {
		assertEquals(false, filterForSessions.accept(new InboxEntry("foo.xml", ".dir1/dir2/foo.xml",
				"file:///tmp/MPS_/S1A/.dir1/dir2/foo.xml", "S1", "A", "MPS_")));
	}

	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue_forSession() {
		assertEquals(true, filterForSessions.accept(new InboxEntry("fooxml", "dir1/dir2/fooxml",
				"file:///tmp/MPS_/S1A/dir1/dir2/fooxml", "S1", "A", "MPS_")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse_forAux() {
		assertEquals(false,
				filterForAux.accept(new InboxEntry("foo.tmp", "foo.tmp", "file:///tmp/AUX/foo.tmp", "", "", "")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse2_forAux() {
		assertEquals(false,
				filterForAux.accept(new InboxEntry(".foo.xml", ".foo.xml", "file:///tmp/AUX/.foo.xml", "", "", "")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse3_forAux() {
		assertEquals(false, filterForAux
				.accept(new InboxEntry("foo.xml", "dir/foo.xml", "file:///tmp/AUX/dir/foo.xml", "", "", "")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse4_forAux() {
		assertEquals(false, filterForAux
				.accept(new InboxEntry("foo", "dir1/dir2/foo", "file:///tmp/AUX/dir1/dir2/foo", "", "", "")));
	}

	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue_forAux() {
		assertEquals(true,
				filterForAux.accept(new InboxEntry("fooxml", "fooxml", "file:///tmp/AUX/fooxml", "", "", "")));
	}
}
