package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import static esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxURIScheme.HTTPS;

import java.nio.file.Path;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class XbipInboxEntryFactory implements InboxEntryFactory {

	@Override
	public InboxEntry newInboxEntry(Path inbox, Path path, int productInDirectory) {
		final InboxEntry inboxEntry = new InboxEntry();
		final Path relativePath = inbox.relativize(path);
		inboxEntry.setName(productName(relativePath, productInDirectory));
		inboxEntry.setRelativePath(relativePath.toString());
		inboxEntry.setPickupPath(HTTPS.getSchemeWithSlashes() + inbox.toString());
		return inboxEntry;
	}

	private final String productName(final Path relativePath, final int productInDirectory) {
		return relativePath.subpath(productInDirectory, relativePath.getNameCount()).toString();
	}

}
