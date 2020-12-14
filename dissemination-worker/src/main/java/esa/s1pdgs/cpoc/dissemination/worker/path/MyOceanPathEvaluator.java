package esa.s1pdgs.cpoc.dissemination.worker.path;

import java.nio.file.Path;
import java.nio.file.Paths;

import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class MyOceanPathEvaluator implements PathEvaluator {
	@Override
	public final Path outputPath(String basePath, ObsObject obsObject) {
		// TODO @MSc: implement
		return Paths.get(basePath, obsObject.getKey().replace(".SAFE", ".ISIP"));
	}

}
