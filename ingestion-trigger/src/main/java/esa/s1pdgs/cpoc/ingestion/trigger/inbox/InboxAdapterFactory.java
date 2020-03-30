package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

public interface InboxAdapterFactory {
	public InboxAdapter newInboxAdapter(String inboxURL, final int productInDirectoryLevel);
}
