package esa.s1pdgs.cpoc.inbox.filter;

import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

public class WhitelistRegexRelativePathInboxFilter implements InboxFilter {
	private final Pattern pattern;

	public WhitelistRegexRelativePathInboxFilter(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean accept(InboxEntry entry) {
		return pattern.matcher(entry.getRelativePath()).matches();
	}

	@Override
	public String toString() {
		return "WhitelistRegexRelativePathInboxFilter [pattern=" + pattern + "]";
	}
}
