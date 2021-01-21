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

		@Override
		public String outputFilename(ObsObject mainFile, ObsObject sourceFile) {
			Path path = Paths.get(sourceFile.getKey()).getFileName();
			if (null == path) {
				throw new RuntimeException(String.format("Cannot create output filename due to corrupt obs object: %s", sourceFile));
			}
			return path.toString();
		}
	};

	public static PathEvaluator newInstance(OutboxConfiguration config) {
		if ("myocean".equalsIgnoreCase(config.getPathEvaluator())) {
			return new MyOceanPathEvaluator();
		}
		return NULL;
	}

	/**
	 * @param basePath  the output base/root path
	 * @param obsObject the object for which the output path is sought
	 * @return the output/destination path for the given object
	 */
	Path outputPath(String basePath, ObsObject obsObject);

	/**
	 * @param mainFile   the main file, which may differ from the source file
	 * @param sourceFile the object for which the output filename is sought
	 * @return the output/destination filename for the given source file
	 */
	String outputFilename(ObsObject mainFile, ObsObject sourceFile);
}
