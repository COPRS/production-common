package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import static esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxURIScheme.FILE;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

@Component
public class FilesystemInboxAdapterFactory implements InboxAdapterFactory {	
	private final InboxEntryFactory inboxEntryFactory;
	
	@Autowired
	public FilesystemInboxAdapterFactory(final FilesystemInboxEntryFactory inboxEntryFactory) {
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
