package esa.s1pdgs.cpoc.ingestion.trigger;

import java.util.Collection;
import java.util.List;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;

public interface InboxAdapter {
	public Collection<InboxEntry> read(final List<InboxFilter> filter);

	public String description();
	
	public String inboxPath();
}
