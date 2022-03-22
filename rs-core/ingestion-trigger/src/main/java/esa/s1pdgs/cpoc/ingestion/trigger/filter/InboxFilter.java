package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

@FunctionalInterface
public interface InboxFilter {
	public static final InboxFilter ALLOW_ALL = (e) -> true;
	public static final InboxFilter ALLOW_NONE = (e) -> false;
	
	static final Logger LOG = LoggerFactory.getLogger(InboxFilter.class);
	
	public boolean accept(InboxEntry entry);	
}
