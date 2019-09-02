package esa.s1pdgs.cpoc.disseminator.path;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public interface PathEvaluater {
	
	public static final PathEvaluater NULL = new PathEvaluater() {		
		@Override
		public String outputPath(ObsObject obsObject) {
			return obsObject.getKey();
		}
	};
	
	public static PathEvaluater newInstance(OutboxConfiguration _config) {
		return NULL;
	}
	
	String outputPath(ObsObject obsObject);
}
