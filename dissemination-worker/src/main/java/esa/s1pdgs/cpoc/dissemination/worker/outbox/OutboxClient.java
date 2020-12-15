package esa.s1pdgs.cpoc.dissemination.worker.outbox;

import java.util.List;

import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.path.PathEvaluator;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public interface OutboxClient {

	public static interface Factory {
		public static final Factory NOT_DEFINED_ERROR = new Factory() {
			@Override
			public OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator eval) {
				throw new RuntimeException(String.format("No OutboxClient.Factory exists for protocol %s", config.getProtocol()));
			}
		};

		OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator eval);
	}

	public static final OutboxClient NULL = new OutboxClient() {
		@Override
		public final String transfer(ObsObject mainFile, List<ObsObject> obsObjects, ReportingFactory reportingFactory)
				throws Exception {
			// do nothing
			return "";
		}
	};

	String transfer(ObsObject mainFile, List<ObsObject> obsObjects, ReportingFactory reportingFactory) throws Exception;
}
