package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.net.URI;

public interface InboxAdapterFactory {
	public InboxAdapter newInboxAdapter(URI inbox, int productInDirectoryLevel, final String stationName);
}
