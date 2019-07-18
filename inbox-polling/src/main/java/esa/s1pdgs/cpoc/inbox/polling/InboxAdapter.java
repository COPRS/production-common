package esa.s1pdgs.cpoc.inbox.polling;

import java.util.Collection;

import esa.s1pdgs.cpoc.inbox.polling.filter.InboxFilter;

public interface InboxAdapter {
	public Collection<InboxEntry> read(final InboxFilter filter);
	public String description();
}
