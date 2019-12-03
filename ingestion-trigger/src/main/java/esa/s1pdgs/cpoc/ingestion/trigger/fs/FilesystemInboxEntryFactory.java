package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

@Component
public class FilesystemInboxEntryFactory implements InboxEntryFactory {	
	@Override
	public InboxEntry newInboxEntry(final Path inbox, final Path path) {		
		final InboxEntry inboxEntry = new InboxEntry();
		inboxEntry.setName(path.toFile().getName());		
		inboxEntry.setRelativePath(inbox.relativize(path).toString());		
		inboxEntry.setPickupPath(inbox.toString());
		return inboxEntry;
	}
}
