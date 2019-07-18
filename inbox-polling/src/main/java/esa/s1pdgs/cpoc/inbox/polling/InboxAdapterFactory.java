package esa.s1pdgs.cpoc.inbox.polling;

public interface InboxAdapterFactory {
	public InboxAdapter newInboxAdapter(String inboxPath);
	
	public InboxEntry newInboxEntry(String inboxPath);
}
