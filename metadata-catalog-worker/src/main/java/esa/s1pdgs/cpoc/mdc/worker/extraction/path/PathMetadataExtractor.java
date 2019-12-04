package esa.s1pdgs.cpoc.mdc.worker.extraction.path;

import java.util.Collections;
import java.util.Map;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public interface PathMetadataExtractor {
	public static final PathMetadataExtractor NULL = new PathMetadataExtractor() {		
		@Override
		public Map<String, String> metadataFrom(final CatalogJob job) {
			return Collections.emptyMap();
		}
	};
	Map<String, String> metadataFrom(CatalogJob job);
}