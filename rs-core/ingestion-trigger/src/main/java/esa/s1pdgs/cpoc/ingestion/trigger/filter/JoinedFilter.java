package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.Arrays;
import java.util.List;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class JoinedFilter implements InboxFilter {	
	
	private final List<InboxFilter> filters;
	
	public JoinedFilter(final InboxFilter ... filters) {
		this(Arrays.asList(filters));
	}

	public JoinedFilter(final List<InboxFilter> filters) {
		this.filters = filters;
	}

	@Override
	public final boolean accept(final InboxEntry entry) {
		for (final InboxFilter filter : filters) {
			if (!filter.accept(entry)) {
				LOG.trace("Entry '{}' is ignored by {}", entry, filter);
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "JoinedFilter [filters=" + filters + "]";
	}
}
