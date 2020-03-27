package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import static esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxURIScheme.HTTPS;

import java.nio.file.Path;
import java.util.Date;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class XbipInboxEntryFactory implements InboxEntryFactory {
	@Override
	public InboxEntry newInboxEntry(
			final Path inbox, 
			final Path path, 
			final int productInDirectory,
			final Date lastModified, 
			final long size
	) {
		final InboxEntry inboxEntry = new InboxEntry();
		final Path relativePath = inbox.relativize(path);
		inboxEntry.setName(productName(relativePath, productInDirectory));
		inboxEntry.setRelativePath(relativePath.toString());
		inboxEntry.setPickupPath(HTTPS.getSchemeWithSlashes() + inbox.toString());
		inboxEntry.setLastModified(lastModified);
		inboxEntry.setSize(size);
		return inboxEntry;
	}

	private final String productName(final Path relativePath, final int productInDirectory) {
		return relativePath.subpath(productInDirectory, relativePath.getNameCount()).toString();
	}
}
