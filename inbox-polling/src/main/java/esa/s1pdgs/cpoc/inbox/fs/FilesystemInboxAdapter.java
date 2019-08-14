package esa.s1pdgs.cpoc.inbox.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	public Collection<InboxEntry> read(List<InboxFilter> filter) {
		final Set<InboxEntry> result = new HashSet<>();

		LOG.trace("Reading inbox filesystem directory '{}'", inboxDirectory);
		try {
			for (final Path entryRelativePath : listDeep()) {
				LOG.trace("Found '{}' in inbox filesystem directory '{}'", entryRelativePath, inboxDirectory);
				final InboxEntry inboxEntry = inboxEntryFactory.newInboxEntry(inboxPathInformation, entryRelativePath,
						inboxDirectory.toPath());
				boolean ignore = false;
				for (InboxFilter f : filter) {
					if (!f.accept(inboxEntry)) {
						LOG.debug("Entry '{}' in inbox filesystem directory '{}' is ignored by {}", inboxEntry,
								inboxDirectory, filter);
						ignore = true;
						break;
					}
				}
				if (ignore) {
					continue;
				}
				LOG.trace("Adding {} from inbox filesystem directory '{}'", inboxEntry);
				result.add(inboxEntry);
			}
		} catch (IOException e) {
			LOG.error("error while listing inbox directory", e);
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

	private final Iterable<Path> listDeep() throws IOException {

		Iterable<Path> list = Files.walk(inboxDirectory.toPath(), FileVisitOption.FOLLOW_LINKS)
				.filter(p -> !p.equals(inboxDirectory.toPath())).map(p -> inboxDirectory.toPath().relativize(p)).collect(Collectors.toList());
		LOG.trace("listing {}", list.toString());
		return list;

	}
}
