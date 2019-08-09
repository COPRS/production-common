package esa.s1pdgs.cpoc.disseminator.outbox;

import com.amazonaws.SdkClientException;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public interface OutboxClient {		
	public static interface Factory {
		OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config);
	}	
	void transfer(ProductFamily family, String keyObjectStorage) throws SdkClientException, ObsException;
}
