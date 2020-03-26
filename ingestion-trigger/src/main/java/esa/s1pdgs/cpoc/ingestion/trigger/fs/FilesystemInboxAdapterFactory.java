package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import static esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxURIScheme.FILE;

import java.io.File;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class FilesystemInboxAdapterFactory implements InboxAdapterFactory {	
	private final InboxEntryFactory inboxEntryFactory;
	
	public FilesystemInboxAdapterFactory(final InboxEntryFactory inboxEntryFactory) {
		this.inboxEntryFactory = inboxEntryFactory;
	}
	
	@Override
	public InboxAdapter newInboxAdapter(final String inboxPath, final int productInDirectoryLevel) {
		return new FilesystemInboxAdapter(
				new File(inboxPath.replace(FILE.getSchemeWithSlashes(), "")).toPath(), 
				inboxEntryFactory,
				productInDirectoryLevel
		);
	}
}
