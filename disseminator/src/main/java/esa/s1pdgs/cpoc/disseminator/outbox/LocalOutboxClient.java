package esa.s1pdgs.cpoc.disseminator.outbox;

import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public final class LocalOutboxClient extends AbstractOutboxClient {	
	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(ObsClient obsClient, OutboxConfiguration config) {
			return new LocalOutboxClient(obsClient, config);
		}
	}
	
	public LocalOutboxClient(ObsClient obsClient, OutboxConfiguration config) {
		super(obsClient, config);
	}

	@Override
	public void transfer(final ObsObject obsObject) throws ObsException {
		obsClient.downloadFile(obsObject.getFamily(), obsObject.getKey(), config.getPath());
	}
}