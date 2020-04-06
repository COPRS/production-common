package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

import static org.junit.Assert.*;

public class TestWhitelistRegexRelativePathInboxFilter {

	private final static String RELATIVE_PATH_1 = "L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00008.raw";
	private final static String RELATIVE_PATH_2 = "L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml";
	private final static String RELATIVE_PATH_3 = "L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00031.raw";
	private final static String RELATIVE_PATH_4 = "L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSIB.xml";

	private final static String SESSION_REGEX = "(.+DSIB\\.(xml|XML)|.+DSDB.*\\.(raw|RAW|aisp|AISP))";

	private final WhitelistRegexRelativePathInboxFilter filterForSessions = new WhitelistRegexRelativePathInboxFilter(
			Pattern.compile(SESSION_REGEX));

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse_forSession() {
		assertFalse(filterForSessions.accept(new InboxEntry("foo.tmp", "dir1/dir2/foo.tmp",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.tmp", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnTrue_forSession() {
		assertTrue(filterForSessions.accept(new InboxEntry("foo.DSDB.raw", "dir1/dir2/foo.DSDB.raw",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.DSDB.raw", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnTrue2_forSession() {
		assertTrue(filterForSessions.accept(new InboxEntry("foo.DSDB_bar.AISP", "dir1/dir2/foo.DSDB_bar.AISP",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.DSDB_bar.AISP", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnTrue3_forSession() {
		assertTrue(filterForSessions.accept(new InboxEntry("foo.DSIB.xml", "dir1/dir2/foo.DSIB.xml",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.DSIB.xml", new Date(), 0)));
	}

	@Test
	public final void testSessionRegex() {
		Assert.assertTrue(Pattern.matches(SESSION_REGEX, RELATIVE_PATH_1));
		Assert.assertTrue(Pattern.matches(SESSION_REGEX, RELATIVE_PATH_2));
		Assert.assertTrue(Pattern.matches(SESSION_REGEX, RELATIVE_PATH_3));
		Assert.assertTrue(Pattern.matches(SESSION_REGEX, RELATIVE_PATH_4));
	}

}
