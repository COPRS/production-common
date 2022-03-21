package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactoryImpl;

public class TestInboxEntry {
	private InboxEntryFactory factory;
	
	private static final Logger LOG = LoggerFactory.getLogger(TestInboxEntry.class);

	@BeforeEach
	public void setup() {
		LOG.info("Run setup method for test.");
		final ProcessConfiguration processConfiguration = new ProcessConfiguration();
		processConfiguration.setHostname("ingestor-01");
		factory = new InboxEntryFactoryImpl(processConfiguration);
	}

	@Test
	public final void testGetName_OnValidName_ShallReturnName() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar");

		Assertions.assertEquals("fooBar", uut.getName());
	}

	@Test
	public final void testHashCode_OnSameObject_ShallReturnSameHashCode() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/fooBar");
		final InboxEntry uut2 = newInboxEntry("/tmp/fooBar");
		Assertions.assertEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testHashCode_OnDifferentObject_ShallReturnDifferentHashCode() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/foo");
		final InboxEntry uut2 = newInboxEntry("/tmp/bar");
		Assertions.assertNotEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testEquals_OnSameObject_ShallReturnTrue() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/foo");
		final InboxEntry uut2 = newInboxEntry("/tmp/foo");
		Assertions.assertEquals(uut1, uut2);
	}

	@Test
	public final void testEquals_OnNull_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar");
		Assertions.assertNotEquals(null, uut);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public final void testEquals_OnDifferentClass_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar2");
		Assertions.assertNotEquals(uut, new File("/tmp/fooBar2"));
	}

	private InboxEntry newInboxEntry(final String path) throws URISyntaxException {
		return factory.newInboxEntry(
				new URI("/tmp"),
				Paths.get(path),
				new Date(),
				0,
				null,
				null,
				null,
				ProductFamily.EDRS_SESSION
		);
	}
}
