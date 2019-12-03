package esa.s1pdgs.cpoc.ingestion.trigger;

import java.io.IOException;
import java.util.Collection;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;

public interface InboxAdapter {
	public Collection<InboxEntry> read(final InboxFilter filter) throws IOException;

	public String description();
	
	public String inboxPath();
}
