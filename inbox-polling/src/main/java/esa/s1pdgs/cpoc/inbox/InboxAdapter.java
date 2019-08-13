package esa.s1pdgs.cpoc.inbox;

import java.util.Collection;
import java.util.List;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.filter.InboxFilter;

public interface InboxAdapter {
	public Collection<InboxEntry> read(final List<InboxFilter> filter);

	public String description();
}
