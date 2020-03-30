package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.net.URI;
import java.net.URISyntaxException;

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
	public InboxAdapter newInboxAdapter(final String inboxURL, final int productInDirectoryLevel) {
		try {
			return new FilesystemInboxAdapter(new URI(inboxURL), inboxEntryFactory, productInDirectoryLevel);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
