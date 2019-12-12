package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxEntryFactory;

public class TestInboxEntry {
	private final FilesystemInboxEntryFactory factory = new FilesystemInboxEntryFactory();

	@Test
	public final void testGetName_OnValidName_ShallReturnName() {
		final InboxEntry uut = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/fooBar"),0);
		assertEquals("fooBar", uut.getName());
	}

	@Test
	public final void testHashCode_OnSameObject_ShallReturnSameHashCode() {
		final InboxEntry uut1 = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/fooBar"),0);
		final InboxEntry uut2 = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/fooBar"),0);
		assertEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testHashCode_OnDifferentObject_ShallReturnDifferentHashCode() {
		final InboxEntry uut1 = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/foo"),0);
		final InboxEntry uut2 = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/bar"),0);
		assertNotEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testEquals_OnSameObject_ShallReturnTrue() {
		final InboxEntry uut1 = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/foo"),0);
		final InboxEntry uut2 = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/foo"),0);
		assertEquals(true, uut1.equals(uut2));
	}

	@Test
	public final void testEquals_OnNull_ShallReturnFalse() {
		final InboxEntry uut = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/fooBar"),0);
		assertEquals(false, uut.equals(null));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public final void testEquals_OnDifferntClass_ShallReturnFalse() {
		final InboxEntry uut = factory.newInboxEntry(Paths.get("/tmp"), Paths.get("/tmp/fooBar2"),0);
		assertEquals(false, uut.equals(new File("/tmp/fooBar2")));
	}
}
