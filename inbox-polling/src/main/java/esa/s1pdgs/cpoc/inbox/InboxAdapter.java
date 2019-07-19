package esa.s1pdgs.cpoc.inbox;

import java.util.Collection;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.filter.InboxFilter;

public interface InboxAdapter {
	public Collection<InboxEntry> read(final InboxFilter filter);
	public String description();
}
