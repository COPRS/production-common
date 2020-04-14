package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.net.URI;
import java.nio.file.Path;
import java.util.Date;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public interface InboxEntryFactory {
	public InboxEntry newInboxEntry(
			URI inboxURL, 
			Path path,
			Date lastModified, 
			long size, 
			String stationName
	);
}
