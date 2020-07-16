package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.io.File;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

@Component
public class FilesystemInboxAdapterFactory implements InboxAdapterFactory {
	private final InboxEntryFactory inboxEntryFactory;

	@Autowired
	public FilesystemInboxAdapterFactory(final InboxEntryFactory inboxEntryFactory) {
		this.inboxEntryFactory = inboxEntryFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(final URI inbox,final String stationName) {
		final File inboxDirectory = new File(inbox.getPath());
		if (!inboxDirectory.exists()) {
			inboxDirectory.mkdirs();
		}	
		return new FilesystemInboxAdapter(inboxEntryFactory, inbox, stationName);
	}
}
