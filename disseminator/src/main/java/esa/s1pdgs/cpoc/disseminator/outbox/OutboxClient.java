package esa.s1pdgs.cpoc.disseminator.outbox;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.Reporting;

public interface OutboxClient {		
	public static interface Factory {
		public static final Factory NOT_DEFINED_ERROR = new Factory() {			
			@Override public OutboxClient newClient(ObsClient obsClient, OutboxConfiguration config, final PathEvaluater eval) {
				throw new RuntimeException(String.format("No OutboxClient.Factory exists for protocol %s", config.getProtocol()));
			}
		}; 
		
		OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluater eval);
	}	
	
	public static final OutboxClient NULL = new OutboxClient() {
		@Override
		public final String transfer(ObsObject obsObject, Reporting.ChildFactory reportingChildFactory) throws Exception {
			// do nothing
			return "";
		}		
	};
	
	String transfer(ObsObject obsObject, Reporting.ChildFactory reportingChildFactory) throws Exception;
}
