package esa.s1pdgs.cpoc.disseminator.path;

import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class IsipPathEvaluater implements PathEvaluater {
	@Override
	public final String outputPath(ObsObject obsObject) {
		return obsObject.getKey().replace(".SAFE", ".ISIP") + "/" + obsObject.getKey();
	}

}
