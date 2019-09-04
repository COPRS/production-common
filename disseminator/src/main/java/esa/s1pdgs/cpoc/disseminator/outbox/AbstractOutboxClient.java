package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public abstract class AbstractOutboxClient implements OutboxClient {	
	protected final Logger logger = LogManager.getLogger(getClass());
	
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
	
	protected final Iterable<Map.Entry<String, InputStream>> entries(final ObsObject obsObject) throws SdkClientException {
		return obsClient.getAllAsInputStream(obsObject.getFamily(), obsObject.getKey()).entrySet();
	}

	protected final void createParentIfRequired(final File file) {
		final File parent = file.getParentFile();
		if (parent == null) {
			throw new RuntimeException(
					String.format("Invalid parent of %s", file)
			);
		}
		mkdirLocal(parent);
	}
	
	protected final Path evaluatePathFor(ObsObject obsObject) {
		final Path path = pathEvaluator.outputPath(config.getPath(), obsObject);	
		mkdirLocal(path.toFile());
		return path;
	}
	
	private final void mkdirLocal(final File file) {
		if (!file.exists()) {
			logger.debug("Creating directory {}", file);
			file.mkdirs();
		}	
	}
}