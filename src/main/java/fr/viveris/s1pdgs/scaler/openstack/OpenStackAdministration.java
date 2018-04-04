package fr.viveris.s1pdgs.scaler.openstack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.scaler.openstack.services.ServerService;

@Service
public class OpenStackAdministration {
	
	private final ServerService serverService;

	@Autowired
	public OpenStackAdministration(final ServerService serverService) {
		this.serverService = serverService;
	}

	public void deleteServer (String serverId) {
		this.serverService.delete(serverId);
	}
}
