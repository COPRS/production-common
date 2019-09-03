package esa.s1pdgs.cpoc.disseminator.outbox;

import java.nio.file.Path;
import java.nio.file.Paths;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public abstract class AbstractOutboxClient implements OutboxClient {
	protected final ObsClient obsClient;
	protected final OutboxConfiguration config;
	private final PathEvaluater pathEvaluator;
	
	public AbstractOutboxClient(ObsClient obsClient, OutboxConfiguration config, final PathEvaluater pathEvaluator) {
		this.obsClient = obsClient;
		this.config = config;
		this.pathEvaluator = pathEvaluator;
	}

	@Override
	public String toString() {
		return "OutboxClient-" + config.getProtocol() ;
	}
	
	protected final Path evaluatePathFor(ObsObject obsObject) {
		final Path remoteDir = Paths.get(config.getPath());
		final String path = pathEvaluator.outputPath(obsObject);
		Utils.assertValidPath(path);
		return remoteDir.resolve(path);
	}
}