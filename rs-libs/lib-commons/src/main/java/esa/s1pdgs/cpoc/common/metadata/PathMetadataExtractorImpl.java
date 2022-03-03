package esa.s1pdgs.cpoc.common.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PathMetadataExtractorImpl implements PathMetadataExtractor {		
	private static final Logger LOG = LogManager.getLogger(PathMetadataExtractorImpl.class);
	
	private final Pattern pattern;
	private final Map<String,Integer> metadataKeyToGroup;
	
	public PathMetadataExtractorImpl(final Pattern pattern, final Map<String, Integer> metadataKeyToGroup) {
		this.pattern = pattern;
		this.metadataKeyToGroup = metadataKeyToGroup;
	}
	
	@Override
	public final Map<String,String> metadataFrom(final String relativePath) {
		final Map<String,String> result = new HashMap<>();
		
		final Matcher matcher = pattern.matcher(relativePath);
		if (matcher.matches()) {
			for (final Map.Entry<String, Integer> entry : metadataKeyToGroup.entrySet()) {
				final String value = matcher.group(entry.getValue());
				LOG.debug("{} evaluates on {} to {}", entry, relativePath, value);
				result.put(entry.getKey(), value);
			}
		}
		return result;			
	}		
}