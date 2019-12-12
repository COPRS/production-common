package esa.s1pdgs.cpoc.ingestion.trigger;

public interface InboxAdapterFactory {
	public InboxAdapter newInboxAdapter(String inboxPath, final int productInDirectoryLevel);
}
