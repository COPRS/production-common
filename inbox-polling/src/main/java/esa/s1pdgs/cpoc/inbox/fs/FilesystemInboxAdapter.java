package esa.s1pdgs.cpoc.inbox.fs;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.inbox.config.InboxPathInformation;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.filter.InboxFilter;

public class FilesystemInboxAdapter implements InboxAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(FilesystemInboxAdapter.class);

	private final InboxEntryFactory inboxEntryFactory;
	private final File inboxDirectory;
	private final InboxPathInformation inboxPathInformation;

	public FilesystemInboxAdapter(final File inboxDirectory, final InboxEntryFactory inboxEntryFactory) {
		this.inboxDirectory = inboxDirectory;
		this.inboxEntryFactory = inboxEntryFactory;
		this.inboxPathInformation = new InboxPathInformation();
		this.inboxPathInformation
				.setMissionId(InboxPathInformation.extractMissionIdFromInboxDirectoryName(inboxDirectory.getPath()));
		this.inboxPathInformation.setSatelliteId(
				InboxPathInformation.extractSatelliteIdFromInboxDirectoryName(inboxDirectory.getPath()));
		this.inboxPathInformation.setStationCode(
				InboxPathInformation.extractStationCodeFromInboxDirectoryName(inboxDirectory.getPath()));
	}

	@Override
	public Collection<InboxEntry> read(InboxFilter filter) {
		final Set<InboxEntry> result = new HashSet<>();

		LOG.trace("Reading inbox filesystem directory '{}'", inboxDirectory);
		for (final File entry : list()) {
			LOG.trace("Found '{}' in inbox filesystem directory '{}'", entry, inboxDirectory);
			final InboxEntry inboxEntry = inboxEntryFactory.newInboxEntry(inboxPathInformation, entry.getPath());
			if (!filter.accept(inboxEntry)) {
				LOG.debug("Entry '{}' in inbox filesystem directory '{}' is ignored by {}", inboxEntry, inboxDirectory,
						filter);
				continue;
			}
			LOG.trace("Adding {} from inbox filesystem directory '{}'", inboxEntry);
			result.add(inboxEntry);
		}
		return result;
	}

	@Override
	public String description() {
		return "Inbox at file://" + inboxDirectory.getPath();
	}

	@Override
	public String toString() {
		return "FilesystemInboxAdapter [inboxDirectory=" + inboxDirectory + "]";
	}

	private final Iterable<File> list() {
		final File[] listing = inboxDirectory.listFiles();

		if (listing == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(listing);
	}
}
