package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class BlacklistRegexRelativePathInboxFilter implements InboxFilter {
	private final Pattern pattern;

	public BlacklistRegexRelativePathInboxFilter(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean accept(InboxEntry entry) {
		return !pattern.matcher(entry.getRelativePath()).matches();
	}

	@Override
	public String toString() {
		return "BlacklistRegexRelativePathInboxFilter [pattern=" + pattern + "]";
	}
}
