package esa.s1pdgs.cpoc.inbox;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

public interface InboxEntryFactory {
	public InboxEntry newInboxEntry(String inboxPath);
}
