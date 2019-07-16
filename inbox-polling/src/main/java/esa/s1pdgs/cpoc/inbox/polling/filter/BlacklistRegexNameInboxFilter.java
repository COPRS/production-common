package esa.s1pdgs.cpoc.inbox.polling.filter;

import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;

public class BlacklistRegexNameInboxFilter implements InboxFilter {
	private final Pattern pattern;
	
	public BlacklistRegexNameInboxFilter(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean accept(InboxEntry entry) {
		return !pattern.matcher(entry.getName()).matches();
	}
}
