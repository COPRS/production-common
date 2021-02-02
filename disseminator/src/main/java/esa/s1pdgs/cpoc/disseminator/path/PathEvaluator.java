package esa.s1pdgs.cpoc.disseminator.path;

import java.nio.file.Path;
import java.nio.file.Paths;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public interface PathEvaluator {	
	public static final PathEvaluator NULL = new PathEvaluator() {		
		@Override
		public Path outputPath(String basePath, ObsObject obsObject) {
			return Paths.get(basePath);
		}
	};
	
	public static PathEvaluator newInstance(OutboxConfiguration config) {		
		if ("ISIP".equalsIgnoreCase(config.getPathEvaluator())) {
			return new IsipPathEvaluator();
		}
		return NULL;
	}
	
	Path outputPath(String basePath, ObsObject obsObject);
}
