package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.Date;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class MinimumModificationDateFilter implements InboxFilter {	
	private final Date ignoreFilesBefore;
	
	public MinimumModificationDateFilter(final Date ignoreFilesBefore) {
		this.ignoreFilesBefore = ignoreFilesBefore;
	}

	@Override
	public final boolean accept(final InboxEntry entry) {
		final Date lastModified = entry.getLastModified();					
		// in doubt (i.e. if last modification date could not be determined) 
		// accept the entry
		if (lastModified == null) {
			return true;
		}
		return lastModified.after(ignoreFilesBefore);
	}

	@Override
	public String toString() {
		return "MinimumModificationDateFilter [ignoreFilesBefore=" + ignoreFilesBefore + "]";
	}
}
