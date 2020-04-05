package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

import static org.junit.Assert.*;

public class TestBlacklistRegexRelativePathInboxFilter {

	private final BlacklistRegexRelativePathInboxFilter filterForSessions = new BlacklistRegexRelativePathInboxFilter(
			Pattern.compile("(^\\..*|^.*/\\..*|.*\\.tmp$|^lost\\+found$)"));

	private final BlacklistRegexRelativePathInboxFilter filterForAux = new BlacklistRegexRelativePathInboxFilter(
			Pattern.compile("(^\\..*|.+/.+|.*\\.tmp$|^lost\\+found$)"));

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse_forSession() {
		assertFalse(filterForSessions.accept(new InboxEntry(0, "foo.tmp", "dir1/dir2/foo.tmp",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.tmp", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse2_forSession() {
		assertFalse(filterForSessions.accept(new InboxEntry(0, ".foo.xml", "dir1/dir2/.foo.xml",
				"file:///tmp/MPS_/S1A/dir1/dir2/.foo.xml", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse3_forSession() {
		assertFalse(filterForSessions.accept(new InboxEntry(0, "foo.xml", ".dir1/dir2/foo.xml",
				"file:///tmp/MPS_/S1A/.dir1/dir2/foo.xml", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue_forSession() {
		assertTrue(filterForSessions.accept(
				new InboxEntry(0, "fooxml", "dir1/dir2/fooxml", "file:///tmp/MPS_/S1A/dir1/dir2/fooxml", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse_forAux() {
		assertFalse(filterForAux.accept(new InboxEntry(0, "foo.tmp", "foo.tmp", "file:///tmp/AUX/foo.tmp", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse2_forAux() {
		assertFalse(filterForAux.accept(new InboxEntry(0, ".foo.xml", ".foo.xml", "file:///tmp/AUX/.foo.xml", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse3_forAux() {
		assertFalse(filterForAux
				.accept(new InboxEntry(0, "foo.xml", "dir/foo.xml", "file:///tmp/AUX/dir/foo.xml", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse4_forAux() {
		assertFalse(filterForAux
				.accept(new InboxEntry(0, "foo", "dir1/dir2/foo", "file:///tmp/AUX/dir1/dir2/foo", new Date(), 0)));
	}

	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue_forAux() {
		assertTrue(filterForAux.accept(new InboxEntry(0, "fooxml", "fooxml", "file:///tmp/AUX/fooxml", new Date(), 0)));
	}
}
