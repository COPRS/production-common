package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.InboxEntryFactory;

@Component
public class FilesystemInboxAdapterFactory implements InboxAdapterFactory {	
	private final InboxEntryFactory inboxEntryFactory;
	
	@Autowired
	public FilesystemInboxAdapterFactory(final InboxEntryFactory inboxEntryFactory) {
		this.inboxEntryFactory = inboxEntryFactory;
	}
	
	@Override
	public InboxAdapter newInboxAdapter(final String inboxPath, final int productInDirectoryLevel) {
		return new FilesystemInboxAdapter(
				new File(inboxPath.replace("file://", "")).toPath(), 
				inboxEntryFactory,
				productInDirectoryLevel
		);
	}
}
