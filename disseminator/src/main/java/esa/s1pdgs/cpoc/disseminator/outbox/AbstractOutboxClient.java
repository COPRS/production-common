package esa.s1pdgs.cpoc.disseminator.outbox;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public abstract class AbstractOutboxClient implements OutboxClient {
	protected final ObsClient obsClient;
	protected final OutboxConfiguration config;
	
	public AbstractOutboxClient(ObsClient obsClient, OutboxConfiguration config) {
		this.obsClient = obsClient;
		this.config = config;
	}

	@Override
	public String toString() {
		return "OutboxClient-" + config.getProtocol() ;
	}
}