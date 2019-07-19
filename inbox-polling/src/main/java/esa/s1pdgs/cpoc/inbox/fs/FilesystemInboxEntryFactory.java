package esa.s1pdgs.cpoc.inbox.fs;

import java.io.File;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

@Component
public class FilesystemInboxEntryFactory implements InboxEntryFactory {
	@Override
	public InboxEntry newInboxEntry(String inboxPath) {		
		final File file = new File(inboxPath.replace("file://", ""));		
		return new InboxEntry(file.getName(),"file://" + file.getPath());
	}	
}
