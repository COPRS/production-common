package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import static esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxURIScheme.FILE;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
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

public class FilesystemInboxAdapter implements InboxAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(FilesystemInboxAdapter.class);

	private final InboxEntryFactory inboxEntryFactory;
	private final Path inboxDirectory;
	private final int productInDirectoryLevel;

	public FilesystemInboxAdapter(final Path inboxDirectory, final InboxEntryFactory inboxEntryFactory,
			final int productInDirectoryLevel) {
		this.inboxDirectory = inboxDirectory;
		this.inboxEntryFactory = inboxEntryFactory;
		this.productInDirectoryLevel = productInDirectoryLevel;
	}

	@Override
	public Collection<InboxEntry> read(final InboxFilter filter) throws IOException {
		LOG.trace("Reading inbox filesystem directory '{}'", inboxDirectory);
		final Set<InboxEntry> entries = Files.walk(inboxDirectory, FileVisitOption.FOLLOW_LINKS)
				.filter(p -> !inboxDirectory.equals(p))
				.filter(p -> exceedsMinConfiguredDirectoryDepth(p)).map(p -> newInboxEntryFor(p))
				.filter(e -> filter.accept(e)).collect(Collectors.toSet());
		LOG.trace("Found {} entries in inbox filesystem directory '{}': {}", entries.size(), inboxDirectory, entries);
		return entries;
	}

	@Override
	public String description() {
		return String.format("Inbox at %s%s", FILE.getSchemeWithSlashes(), inboxDirectory);
	}

	@Override
	public String inboxPath() {
		return FILE.getSchemeWithSlashes() + inboxDirectory;
	}

	@Override
	public String toString() {
		return String.format("FilesystemInboxAdapter [inboxDirectory=%s]", inboxDirectory);
	}

	private final InboxEntry newInboxEntryFor(final Path path) {
		return inboxEntryFactory.newInboxEntry(inboxDirectory, path, productInDirectoryLevel);
	}

	private final boolean exceedsMinConfiguredDirectoryDepth(final Path path) {
		return inboxDirectory.relativize(path).getNameCount() > productInDirectoryLevel;
	}
}
