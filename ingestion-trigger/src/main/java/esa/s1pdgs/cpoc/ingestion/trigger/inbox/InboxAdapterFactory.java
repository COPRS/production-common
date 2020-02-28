package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

public interface InboxAdapterFactory {
	public InboxAdapter newInboxAdapter(String inboxPath, final int productInDirectoryLevel);
}
