package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.nio.file.Path;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public interface InboxEntryFactory {
	public InboxEntry newInboxEntry(Path inbox, Path path, int productAt);
}
