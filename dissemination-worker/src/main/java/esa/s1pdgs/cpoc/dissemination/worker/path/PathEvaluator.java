package esa.s1pdgs.cpoc.dissemination.worker.path;

import java.nio.file.Path;
import java.nio.file.Paths;

import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public interface PathEvaluator {
	public static final PathEvaluator NULL = new PathEvaluator() {
		@Override
		public Path outputPath(String basePath, ObsObject obsObject) {
			return Paths.get(basePath);
		}
	};

	public static PathEvaluator newInstance(OutboxConfiguration config) {
		if ("myocean".equalsIgnoreCase(config.getPathEvaluator())) {
			return new MyOceanPathEvaluator();
		}
		return NULL;
	}

	Path outputPath(String basePath, ObsObject obsObject);
}
