package esa.s1pdgs.cpoc.common.metadata;

import java.util.Collections;
import java.util.Map;

public interface PathMetadataExtractor {
	public static final PathMetadataExtractor NULL = new PathMetadataExtractor() {		
		@Override
		public Map<String, String> metadataFrom(final String relativePath) {
			return Collections.emptyMap();
		}
	};
	Map<String, String> metadataFrom(String relativePath);
}