package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Component
public class FilesystemInboxEntryFactory implements InboxEntryFactory {

	@Override
	public InboxEntry newInboxEntry(final URI inboxURL, final Path path, final int productInDirectory,
			final Date lastModified, final long size) {
		final InboxEntry inboxEntry = new InboxEntry();
		final Path relativePath = Paths.get(inboxURL.getPath()).relativize(path);
		inboxEntry.setName(productName(relativePath, productInDirectory));
		inboxEntry.setRelativePath(relativePath.toString());
		inboxEntry.setPickupURL(inboxURL.toString());
		inboxEntry.setLastModified(lastModified);
		inboxEntry.setSize(size);
		return inboxEntry;
	}

	private String productName(final Path relativePath, final int productInDirectory) {
		return relativePath.subpath(productInDirectory, relativePath.getNameCount()).toString();
	}
}
