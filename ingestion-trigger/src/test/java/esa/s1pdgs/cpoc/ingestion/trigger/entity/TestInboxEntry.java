package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxEntryFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestInboxEntry {

	@Autowired
	private FilesystemInboxEntryFactory factory;

	@Test
	public final void testGetName_OnValidName_ShallReturnName() throws URISyntaxException {
		final InboxEntry uut = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar"), 0, new Date(), 0);
		assertEquals("fooBar", uut.getName());
	}

	@Test
	public final void testHashCode_OnSameObject_ShallReturnSameHashCode() throws URISyntaxException {
		final InboxEntry uut1 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar"), 0, new Date(), 0);
		final InboxEntry uut2 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar"), 0, new Date(), 0);
		assertEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testHashCode_OnDifferentObject_ShallReturnDifferentHashCode() throws URISyntaxException {
		final InboxEntry uut1 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/foo"), 0, new Date(), 0);
		final InboxEntry uut2 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/bar"), 0, new Date(), 0);
		assertNotEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testEquals_OnSameObject_ShallReturnTrue() throws URISyntaxException {
		final InboxEntry uut1 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/foo"), 0, new Date(), 0);
		final InboxEntry uut2 = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/foo"), 0, new Date(), 0);
		assertTrue(uut1.equals(uut2));
	}

	@Test
	public final void testEquals_OnNull_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar"), 0, new Date(), 0);
		assertFalse(uut.equals(null));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public final void testEquals_OnDifferntClass_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = factory.newInboxEntry(new URI("/tmp"), Paths.get("/tmp/fooBar2"), 0, new Date(), 0);
		assertFalse(uut.equals(new File("/tmp/fooBar2")));
	}
}
