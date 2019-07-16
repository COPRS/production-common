package esa.s1pdgs.cpoc.inbox.polling.filter;

import java.util.Set;

import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;

public class BlacklistFilenameInboxFilter implements InboxFilter {
	private final Set<String> names;
	
	public BlacklistFilenameInboxFilter(Set<String> names) {
		this.names = names;
	}

	@Override
	public boolean accept(InboxEntry entry) {
		return !names.contains(entry.getName());
	}
}
