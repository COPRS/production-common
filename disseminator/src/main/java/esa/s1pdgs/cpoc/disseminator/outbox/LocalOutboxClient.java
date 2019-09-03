package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.File;
import java.nio.file.Path;

import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public final class LocalOutboxClient extends AbstractOutboxClient {	
	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(ObsClient obsClient, OutboxConfiguration config, final PathEvaluater eval) {
			return new LocalOutboxClient(obsClient, config, eval);
		}
	}
	
	public LocalOutboxClient(ObsClient obsClient, OutboxConfiguration config, final PathEvaluater eval) {
		super(obsClient, config, eval);
	}

	@Override
	public final void transfer(final ObsObject obsObject) throws ObsException, ObsServiceException, SdkClientException {		
		final Path path = evaluatePathFor(obsObject);	
		
		final Path parentPath = path.getParent();
		
		if (parentPath == null) {
			throw new RuntimeException("Invalid parent path in " + path);
		}
		
		final File parent = parentPath.toFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}		
		obsClient.downloadObject(new ObsDownloadObject(obsObject.getKey(), obsObject.getFamily(), parent.getPath()));
	}
}