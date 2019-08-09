package esa.s1pdgs.cpoc.disseminator.outbox;

import com.amazonaws.SdkClientException;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public final class SftpOutboxClient extends AbstractOutboxClient {
	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(ObsClient obsClient, OutboxConfiguration config) {
			return new SftpOutboxClient(obsClient, config);
		}			
	}
	
	public SftpOutboxClient(ObsClient obsClient, OutboxConfiguration config) {
		super(obsClient, config);
	}

	@Override
	public void transfer(ProductFamily family, String keyObjectStorage) throws SdkClientException, ObsException {
		// TODO Auto-generated method stub
		
	}		
}