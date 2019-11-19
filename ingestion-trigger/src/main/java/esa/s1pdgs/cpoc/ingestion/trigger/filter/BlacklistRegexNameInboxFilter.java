package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class BlacklistRegexNameInboxFilter implements InboxFilter {
	private final Pattern pattern;
	
	public BlacklistRegexNameInboxFilter(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean accept(InboxEntry entry) {
		return !pattern.matcher(entry.getName()).matches();
	}

	@Override
	public String toString() {
		return "BlacklistRegexNameInboxFilter [pattern=" + pattern + "]";
	}
}
