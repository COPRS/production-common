package esa.s1pdgs.cpoc.inbox.polling.fs;

import java.io.File;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.polling.InboxAdapter;
import esa.s1pdgs.cpoc.inbox.polling.InboxAdapterFactory;
import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;

@Component
public class FilesystemInboxAdapterFactory implements InboxAdapterFactory {
	@Override
	public InboxAdapter newInboxAdapter(String inboxPath) {
		return new FilesystemInboxAdapter(new File(inboxPath));
	}

	@Override
	public InboxEntry newInboxEntry(String inboxPath) {
		return new FilesystemInboxEntry(new File(inboxPath));
	}
	
}
