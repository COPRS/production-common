package esa.s1pdgs.cpoc.dissemination.worker.path;

import java.nio.file.Path;
import java.nio.file.Paths;

import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class MyOceanPathEvaluator implements PathEvaluator {
	@Override
	public Path outputPath(final String basePath, final ObsObject obsObject) {
		// TODO @MSc: implement

		return Paths.get(basePath, obsObject.getKey());
	}

	@Override
	public String outputFilename(final ObsObject mainFile, final ObsObject sourceFile) {
		final String sourceFilename = this.getFilename(sourceFile);

		if (sourceFilename.contains("manifest")) {
			// renaming to [mainFilename].manifest
			final String mainFilename = this.getFilename(mainFile);
			return mainFilename + ".manifest";
		} else {
			return sourceFilename;
		}
	}

	// --------------------------------------------------------------------------

	private String getFilename(final ObsObject file) {
		return Paths.get(file.getKey()).getFileName().toString();
	}

}
