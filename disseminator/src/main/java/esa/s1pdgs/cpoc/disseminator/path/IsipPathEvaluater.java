package esa.s1pdgs.cpoc.disseminator.path;

import java.nio.file.Path;
import java.nio.file.Paths;

import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class IsipPathEvaluater implements PathEvaluater {
	@Override
	public final Path outputPath(String basePath, ObsObject obsObject) {
		return Paths.get(basePath, obsObject.getKey().replace(".SAFE", ".ISIP"));
	}

}
