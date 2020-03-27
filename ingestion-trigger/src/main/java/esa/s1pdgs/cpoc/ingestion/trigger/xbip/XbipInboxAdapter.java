package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import static esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxURIScheme.HTTPS;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;

public class XbipInboxAdapter implements InboxAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(XbipInboxAdapter.class);

	private final InboxEntryFactory inboxEntryFactory;
	private final Path inboxDirectory;
	private final int productInDirectoryLevel;
	private final XbipClient xbipClient;

	public XbipInboxAdapter(
			final Path inboxDirectory, 
			final XbipClient xbipClient, 
			final InboxEntryFactory inboxEntryFactory,
			final int productInDirectoryLevel
	) {

		this.inboxDirectory = inboxDirectory;
		this.xbipClient = xbipClient;
		this.inboxEntryFactory = inboxEntryFactory;
		this.productInDirectoryLevel = productInDirectoryLevel;
	}

	@Override
	public Collection<InboxEntry> read(final InboxFilter filter) throws IOException {

		LOG.trace("Reading inbox XBIP directory '{}'", inboxDirectory);
		final Set<InboxEntry> entries = xbipClient.list(XbipEntryFilter.ALLOW_ALL).stream()
				.filter(x -> !inboxDirectory.equals(x.getPath()))
				.filter(x -> exceedsMinConfiguredDirectoryDepth(x.getPath()))
				.map(x -> newInboxEntryFor(x))
				.filter(e -> filter.accept(e)).collect(Collectors.toSet());

		LOG.trace("Found {} entries in inbox XBIP directory '{}': {}", entries.size(), inboxDirectory, entries);
		return entries;
	}

	@Override
	public String description() {
		return String.format("Inbox at %s%s", HTTPS.getSchemeWithSlashes(), inboxDirectory);
	}

	@Override
	public String inboxPath() {
		return HTTPS.getSchemeWithSlashes() + inboxDirectory;
	}

	@Override
	public String toString() {
		return String.format("XbipInboxAdapter [inboxDirectory=%s]", inboxDirectory);
	}

	private final InboxEntry newInboxEntryFor(final XbipEntry xbipEntry) {
		return inboxEntryFactory.newInboxEntry(
				inboxDirectory, 
				xbipEntry.getPath(), 
				productInDirectoryLevel,
				xbipEntry.getLastModified(), 
				xbipEntry.getSize()
		);
	}

	private final boolean exceedsMinConfiguredDirectoryDepth(final Path path) {
		return inboxDirectory.relativize(path).getNameCount() > productInDirectoryLevel;
	}
}
