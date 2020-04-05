package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import esa.s1pdgs.cpoc.common.mongodb.sequence.SequenceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

@Component
public class FilesystemInboxEntryFactory implements InboxEntryFactory {

	@Autowired
	private SequenceDao sequence;

	@Override
	public InboxEntry newInboxEntry(final URI inboxURL, final Path path, final int productInDirectory,
			final Date lastModified, final long size) {
		final InboxEntry inboxEntry = new InboxEntry();
		final Path relativePath = Paths.get(inboxURL.getPath()).relativize(path);
		inboxEntry.setId(sequence.getNextSequenceId(InboxEntry.ENTRY_SEQ_KEY));
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
