package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.io.IOException;
import java.util.List;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;

public interface InboxAdapter {
	List<InboxEntry> read(final InboxFilter filter) throws IOException;

	void advanceAfterPublish();

	String description();
	
	String inboxURL();
}
