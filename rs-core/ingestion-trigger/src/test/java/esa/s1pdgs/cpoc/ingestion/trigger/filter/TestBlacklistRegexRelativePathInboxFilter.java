package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

import static org.junit.Assert.*;

public class TestBlacklistRegexRelativePathInboxFilter {

	private final BlacklistRegexRelativePathInboxFilter filterForSessions = new BlacklistRegexRelativePathInboxFilter(
			Pattern.compile("(^\\..*|^.*/\\..*|.*\\.tmp$|^lost\\+found$)"));

	private final BlacklistRegexRelativePathInboxFilter filterForAux = new BlacklistRegexRelativePathInboxFilter(
			Pattern.compile("(^\\..*|.+/.+|.*\\.tmp$|^lost\\+found$)"));

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse_forSession() {
		assertFalse(this.filterForSessions.accept(new InboxEntry("foo.tmp", "dir1/dir2/foo.tmp",
				"file:///tmp/MPS_/S1A/dir1/dir2/foo.tmp", new Date(), 0, null, null, ProductFamily.EDRS_SESSION.name(), null, null)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse2_forSession() {
		assertFalse(this.filterForSessions.accept(new InboxEntry(".foo.xml", "dir1/dir2/.foo.xml",
				"file:///tmp/MPS_/S1A/dir1/dir2/.foo.xml", new Date(), 0, null, null, ProductFamily.EDRS_SESSION.name(), null, null)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse3_forSession() {
		assertFalse(this.filterForSessions.accept(new InboxEntry("foo.xml", ".dir1/dir2/foo.xml",
				"file:///tmp/MPS_/S1A/.dir1/dir2/foo.xml", new Date(), 0, null, null, ProductFamily.EDRS_SESSION.name(), null, null)));
	}

	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue_forSession() {
		assertTrue(this.filterForSessions
				.accept(new InboxEntry("fooxml", "dir1/dir2/fooxml", "file:///tmp/MPS_/S1A/dir1/dir2/fooxml",
						new Date(), 0, null, null, ProductFamily.EDRS_SESSION.name(), null, null)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse_forAux() {
		assertFalse(this.filterForAux.accept(new InboxEntry("foo.tmp", "foo.tmp", "file:///tmp/AUX/foo.tmp", new Date(),
				0, null, null, ProductFamily.AUXILIARY_FILE.name(), null, null)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse2_forAux() {
		assertFalse(this.filterForAux.accept(new InboxEntry(".foo.xml", ".foo.xml", "file:///tmp/AUX/.foo.xml",
				new Date(), 0, null, null, ProductFamily.AUXILIARY_FILE.name(), null, null)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse3_forAux() {
		assertFalse(this.filterForAux
				.accept(new InboxEntry("foo.xml", "dir/foo.xml", "file:///tmp/AUX/dir/foo.xml", new Date(), 0, null,
						null, ProductFamily.AUXILIARY_FILE.name(), null, null)));
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnFalse4_forAux() {
		assertFalse(this.filterForAux
				.accept(new InboxEntry("foo", "dir1/dir2/foo", "file:///tmp/AUX/dir1/dir2/foo", new Date(), 0, null,
						null, ProductFamily.AUXILIARY_FILE.name(), null, null)));
	}

	@Test
	public final void testAccept_OnNonMatchingRegex_ShallReturnTrue_forAux() {
		assertTrue(this.filterForAux.accept(new InboxEntry("fooxml", "fooxml", "file:///tmp/AUX/fooxml", new Date(), 0,
				null, null, ProductFamily.AUXILIARY_FILE.name(), null, null)));
	}
}
