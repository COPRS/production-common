package esa.s1pdgs.cpoc.inbox.filter;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

public class TestWhitelistRegexRelativePathInboxFilter {

	private final WhitelistRegexRelativePathInboxFilter filterForSessions = new WhitelistRegexRelativePathInboxFilter(
			Pattern.compile("(.+DSIB\\.(xml|XML)|.+DSDB.*\\.(raw|RAW|aisp|AISP))"));

	private final WhitelistRegexRelativePathInboxFilter filterForAux = new WhitelistRegexRelativePathInboxFilter(
			Pattern.compile(".+"));

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse_forSession() {
		assertEquals(false, filterForSessions.accept(new InboxEntry("foo.tmp", "dir1/dir2/foo.tmp",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.tmp", "S1", "A", "MPS_")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnTrue_forSession() {
		assertEquals(true, filterForSessions.accept(new InboxEntry("foo.DSDB.raw", "dir1/dir2/foo.DSDB.raw",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.DSDB.raw", "S1", "A", "MPS_")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnTrue2_forSession() {
		assertEquals(true, filterForSessions.accept(new InboxEntry("foo.DSDB_bar.AISP", "dir1/dir2/foo.DSDB_bar.AISP",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.DSDB_bar.AISP", "S1", "A", "MPS_")));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnTrue3_forSession() {
		assertEquals(true, filterForSessions.accept(new InboxEntry("foo.DSIB.xml", "dir1/dir2/foo.DSIB.xml",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.DSIB.xml", "S1", "A", "MPS_")));
	}

}
