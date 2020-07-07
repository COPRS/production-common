package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactoryImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestInboxEntry {
	private final InboxEntryFactory factory = new InboxEntryFactoryImpl();

	@Test
	public final void testGetName_OnValidName_ShallReturnName() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar");

		assertEquals("fooBar", uut.getName());
	}

	@Test
	public final void testHashCode_OnSameObject_ShallReturnSameHashCode() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/fooBar");
		final InboxEntry uut2 = newInboxEntry("/tmp/fooBar");
		assertEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testHashCode_OnDifferentObject_ShallReturnDifferentHashCode() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/foo");
		final InboxEntry uut2 = newInboxEntry("/tmp/bar");
		assertNotEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testEquals_OnSameObject_ShallReturnTrue() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/foo");
		final InboxEntry uut2 = newInboxEntry("/tmp/foo");
		assertEquals(uut1, uut2);
	}

	@Test
	public final void testEquals_OnNull_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar");
		assertNotEquals(null, uut);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public final void testEquals_OnDifferentClass_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar2");
		assertNotEquals(uut, new File("/tmp/fooBar2"));
	}

	private InboxEntry newInboxEntry(final String path) throws URISyntaxException {
		return factory.newInboxEntry(
				new URI("/tmp"),
				Paths.get(path),
				new Date(),
				0,
				null
		);
	}
}
