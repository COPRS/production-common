package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;

@Component
public class FilesystemInboxAdapterFactory implements InboxAdapterFactory {
	private final FilesystemInboxEntryFactory inboxEntryFactory;

	@Autowired
	public FilesystemInboxAdapterFactory(final FilesystemInboxEntryFactory inboxEntryFactory) {
		this.inboxEntryFactory = inboxEntryFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(final URI inbox, final int productInDirectoryLevel,	final String stationName) {
		return new FilesystemInboxAdapter(inboxEntryFactory, inbox, productInDirectoryLevel, stationName);
	}
}
