package esa.s1pdgs.cpoc.dissemination.worker.outbox;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.path.PathEvaluator;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public abstract class AbstractOutboxClient implements OutboxClient {

	protected final Logger logger = LogManager.getLogger(this.getClass());

	protected final ObsClient obsClient;
	protected final OutboxConfiguration config;
	protected final PathEvaluator pathEvaluator;

	// --------------------------------------------------------------------------

	public AbstractOutboxClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator pathEvaluator) {
		this.obsClient = obsClient;
		this.config = config;
		this.pathEvaluator = pathEvaluator;
	}

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return "OutboxClient-" + this.config.getProtocol();
	}

	protected final Iterable<String> entries(final ObsObject obsObject) throws SdkClientException {
		return this.obsClient.list(obsObject.getFamily(), obsObject.getKey());
	}

	protected final InputStream stream(final ProductFamily family, final String key) throws SdkClientException {
		return this.obsClient.getAsStream(family, key);
	}

	protected final void createParentIfRequired(final File file) {
		final File parent = file.getParentFile();

		if (parent == null) {
			throw new RuntimeException(String.format("Invalid parent of %s", file));
		}

		this.mkdirLocal(parent);
	}

	protected Path evaluatePathFor(final ObsObject obsObject) {
		final Path path = this.pathEvaluator.outputPath(this.config.getPath(), obsObject);
		this.mkdirLocal(path.toFile());

		return path;
	}

	private void mkdirLocal(final File file) {
		if (!file.exists()) {
			this.logger.debug("Creating directory {}", file);
			file.mkdirs();
		}
	}

}