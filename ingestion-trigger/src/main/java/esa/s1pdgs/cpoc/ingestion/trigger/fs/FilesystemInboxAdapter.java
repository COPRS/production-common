package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class FilesystemInboxAdapter implements InboxAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(FilesystemInboxAdapter.class);

	private final InboxEntryFactory inboxEntryFactory;
	private final URI inboxURL;
	private final int productInDirectoryLevel;

	public FilesystemInboxAdapter(final URI inboxURL, final InboxEntryFactory inboxEntryFactory,
			final int productInDirectoryLevel) {
		this.inboxURL = inboxURL;
		this.inboxEntryFactory = inboxEntryFactory;
		this.productInDirectoryLevel = productInDirectoryLevel;
	}

	@Override
	public Collection<InboxEntry> read(final InboxFilter filter) throws IOException {
		LOG.trace("Reading inbox filesystem directory '{}'", inboxURL.toString());
		final Set<InboxEntry> entries = Files.walk(Paths.get(inboxURL.getPath()), FileVisitOption.FOLLOW_LINKS)
				.filter(p -> !Paths.get(inboxURL.getPath()).equals(p))
				.filter(p -> exceedsMinConfiguredDirectoryDepth(p)).map(p -> newInboxEntryFor(p))
				.filter(e -> filter.accept(e)).collect(Collectors.toSet());
		LOG.trace("Found {} entries in inbox filesystem directory '{}': {}", entries.size(), inboxURL.toString(), entries);
		return entries;
	}

	@Override
	public String description() {
		return String.format("Inbox at %s", inboxURL.toString());
	}

	@Override
	public String inboxURL() {
		return inboxURL.toString();
	}

	@Override
	public String toString() {
		return String.format("FilesystemInboxAdapter [inboxDirectory=%s]", inboxURL.toString());
	}

	private final InboxEntry newInboxEntryFor(final Path path) {
		
		File file = path.toFile();
		Date lastModified = new Date(file.lastModified());
		long size = FileUtils.sizeOf(file);
		
		return inboxEntryFactory.newInboxEntry(inboxURL, path, productInDirectoryLevel, lastModified, size);
	}

	private final boolean exceedsMinConfiguredDirectoryDepth(final Path path) {
		return  Paths.get(inboxURL.getPath()).relativize(path).getNameCount() > productInDirectoryLevel;
	}
}
