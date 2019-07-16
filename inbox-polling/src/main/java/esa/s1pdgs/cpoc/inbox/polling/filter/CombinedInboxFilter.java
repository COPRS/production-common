package esa.s1pdgs.cpoc.inbox.polling.filter;

import java.util.List;

import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;

/**
 * Every single provided filter must accept the entry to accept it
 */
public class CombinedInboxFilter implements InboxFilter {
	private final List<InboxFilter> nestedFilters;
	
	public CombinedInboxFilter(List<InboxFilter> nestedFilters) {
		this.nestedFilters = nestedFilters;
	}

	@Override
	public boolean accept(InboxEntry entry) {
		for (final InboxFilter filter : nestedFilters) {
			if (!filter.accept(entry)) {
				return false;
			}
		}
		return true;
	}	
}
