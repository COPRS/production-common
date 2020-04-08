package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxEntryFactory;

public class TestInboxEntry {
	private final FilesystemInboxEntryFactory factory = new FilesystemInboxEntryFactory();

	@Test
	public final void testGetName_OnValidName_ShallReturnName() throws URISyntaxException {
		final InboxEntry uut = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar"), 0, new Date(), 0, null);
		assertEquals("fooBar", uut.getName());
	}

	@Test
	public final void testHashCode_OnSameObject_ShallReturnSameHashCode() throws URISyntaxException {
		final InboxEntry uut1 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar"), 0, new Date(), 0, null);
		final InboxEntry uut2 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar"), 0, new Date(), 0, null);
		assertEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testHashCode_OnDifferentObject_ShallReturnDifferentHashCode() throws URISyntaxException {
		final InboxEntry uut1 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/foo"), 0, new Date(), 0, null);
		final InboxEntry uut2 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/bar"), 0, new Date(), 0, null);
		assertNotEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testEquals_OnSameObject_ShallReturnTrue() throws URISyntaxException {
		final InboxEntry uut1 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/foo"), 0, new Date(), 0, null);
		final InboxEntry uut2 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/foo"), 0, new Date(), 0, null);
		assertEquals(true, uut1.equals(uut2));
	}

	@Test
	public final void testEquals_OnNull_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar"), 0, new Date(), 0, null);
		assertEquals(false, uut.equals(null));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public final void testEquals_OnDifferntClass_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar2"), 0, new Date(), 0, null);
		assertEquals(false, uut.equals(new File("/tmp/fooBar2")));
	}
}
