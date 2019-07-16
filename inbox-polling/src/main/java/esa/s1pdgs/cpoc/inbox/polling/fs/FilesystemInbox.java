package esa.s1pdgs.cpoc.inbox.polling.fs;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.inbox.polling.Inbox;
import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;
import esa.s1pdgs.cpoc.inbox.polling.filter.InboxFilter;

public class FilesystemInbox implements Inbox {
	private static final Logger LOG = LoggerFactory.getLogger(FilesystemInbox.class);
	
	private final File inboxDirectory;

	public FilesystemInbox(File inboxDirectory) {
		this.inboxDirectory = inboxDirectory;
	}

	@Override
	public Collection<InboxEntry> read(InboxFilter filter) {
		final Set<InboxEntry> result = new HashSet<>();
		
		LOG.trace("Reading inbox filesystem directory '{}'", inboxDirectory);
		for (final File entry : list()) {
			LOG.trace("Found '{}' in inbox filesystem directory '{}'", entry, inboxDirectory);
			final InboxEntry inboxEntry = new FilesystemInboxEntry(entry);
			if (!filter.accept(inboxEntry)) {
				LOG.debug("Entry '{}' in inbox filesystem directory '{}' is ignored by {}", inboxEntry, inboxDirectory, filter);
				continue;
			}
			LOG.trace("Adding {} from inbox filesystem directory '{}'", inboxEntry);
			result.add(inboxEntry);
		}		
		return result;
	}
	
	private final Iterable<File> list()
	{
		final File[] listing = inboxDirectory.listFiles();
		
		if (listing == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(listing);		
	}

}
