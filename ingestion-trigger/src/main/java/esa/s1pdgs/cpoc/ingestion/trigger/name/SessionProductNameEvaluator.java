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
		
		// if the pattern does not match, it is an invalid file
		// but we cannot fail here as also the invalid file will be traced so we are simply 
		if (!matcher.matches()) {

		}
		return matcher.group(groupIndex);
	}

}
