package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import static esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxURIScheme.FILE;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

@Component
public class FilesystemInboxEntryFactory implements InboxEntryFactory {
	@Override
	public InboxEntry newInboxEntry(final Path inbox, final Path path, final int productInDirectory) {
		final InboxEntry inboxEntry = new InboxEntry();

		final Path relativePath = inbox.relativize(path);
		inboxEntry.setName(productName(relativePath, productInDirectory));
		inboxEntry.setRelativePath(relativePath.toString());
		inboxEntry.setPickupPath(FILE.getSchemeWithSlashes() + inbox.toString());
		return inboxEntry;
	}

	private final String productName(final Path relativePath, final int productInDirectory) {
		return relativePath.subpath(productInDirectory, relativePath.getNameCount()).toString();
	}
}
