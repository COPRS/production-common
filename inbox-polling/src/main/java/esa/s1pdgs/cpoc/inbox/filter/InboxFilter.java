package esa.s1pdgs.cpoc.inbox.filter;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

@FunctionalInterface
public interface InboxFilter {
	public static final InboxFilter ALLOW_ALL = (e) -> true;
	public static final InboxFilter ALLOW_NONE = (e) -> false;

	public boolean accept(InboxEntry entry);	
}
