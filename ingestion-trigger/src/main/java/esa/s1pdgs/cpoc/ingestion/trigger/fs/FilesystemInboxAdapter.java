package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;

public class FilesystemInboxAdapter implements InboxAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(FilesystemInboxAdapter.class);

	private final InboxEntryFactory inboxEntryFactory;
	private final Path inboxDirectory;

	public FilesystemInboxAdapter(final Path inboxDirectory, final InboxEntryFactory inboxEntryFactory) {
		this.inboxDirectory = inboxDirectory;
		this.inboxEntryFactory = inboxEntryFactory;
	}

	@Override
	public Collection<InboxEntry> read(final InboxFilter filter) throws IOException {
		LOG.trace("Reading inbox filesystem directory '{}'", inboxDirectory);
		final Set<InboxEntry> entries = Files.walk(inboxDirectory, FileVisitOption.FOLLOW_LINKS)
				.filter(p -> !p.equals(inboxDirectory))
				.map(p -> inboxEntryFactory.newInboxEntry(inboxDirectory, p))
				.filter(e -> filter.accept(e))
				.collect(Collectors.toSet());			
		
		LOG.trace("Found {} entries in inbox filesystem directory '{}'", entries.size(), inboxDirectory);
		return entries;
	}

	@Override
	public String description() {
		return "Inbox at file://" + inboxDirectory;
	}
	
	@Override
	public String inboxPath() {
		return inboxDirectory.toString();
	}

	@Override
	public String toString() {
		return "FilesystemInboxAdapter [inboxDirectory=" + inboxDirectory + "]";
	}
}
