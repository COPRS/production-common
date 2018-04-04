package fr.viveris.s1pdgs.scaler.openstack.services;

import org.openstack4j.api.OSClient.OSClientV3;

public class ServerService {
	
	private final OSClientV3 osClient;

	public ServerService(final OSClientV3 osClient) {
		this.osClient = osClient;
	}
	
	public void delete(String serverId) {
		osClient.compute().servers().delete(serverId);
	}

}
