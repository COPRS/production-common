package esa.s1pdgs.cpoc.ingestion.trigger.name;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionProductNameEvaluator implements ProductNameEvaluator {
	private final Pattern pattern;
	private final int groupIndex;
	
	public SessionProductNameEvaluator(final Pattern pattern, final int groupIndex) {
		this.pattern = pattern;
		this.groupIndex = groupIndex;
	}

	@Override
	public String evaluateFrom(final Path relativePath) {
		final Matcher matcher = pattern.matcher(relativePath.toString());

		if (!matcher.matches()) {
			throw new IllegalArgumentException(
					String.format(
							"Could not match relative path %s against regex %s", 
							relativePath,
							pattern
					)
			);
		}
		return matcher.group(groupIndex);
	}

}
