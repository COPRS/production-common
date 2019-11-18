package esa.s1pdgs.cpoc.inbox.fs;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.inbox.InboxEntryFactory;

@Component
public class FilesystemInboxAdapterFactory implements InboxAdapterFactory {	
	private final InboxEntryFactory inboxEntryFactory;
	
	@Autowired
	public FilesystemInboxAdapterFactory(InboxEntryFactory inboxEntryFactory) {
		this.inboxEntryFactory = inboxEntryFactory;
	}
	
	@Override
	public InboxAdapter newInboxAdapter(String inboxPath) {
		return new FilesystemInboxAdapter(new File(inboxPath.replace("file://", "")), inboxEntryFactory);
	}
}
