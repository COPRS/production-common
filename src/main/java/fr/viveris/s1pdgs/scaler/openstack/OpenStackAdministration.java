package fr.viveris.s1pdgs.scaler.openstack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.scaler.openstack.model.ServerDesc;
import fr.viveris.s1pdgs.scaler.openstack.model.VolumeDesc;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsServerException;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsVolumeException;
import fr.viveris.s1pdgs.scaler.openstack.services.ServerService;
import fr.viveris.s1pdgs.scaler.openstack.services.VolumeService;

@Service
public class OpenStackAdministration {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenStackAdministration.class);

	private final OpenStackServerProperties osProperties;

	private final ServerService serverService;

	private final VolumeService volumeService;

	@Autowired
	public OpenStackAdministration(final OpenStackServerProperties osProperties, final ServerService serverService,
			final VolumeService volumeService) {
		this.osProperties = osProperties;
		this.serverService = serverService;
		this.volumeService = volumeService;
	}

	public void deleteServer(String serverId) {
		this.serverService.delete(serverId);
	}

	public String createServerForL1Wrappers(String logPrefix) throws OsVolumeException, OsServerException {
		long currentTimestamp = System.currentTimeMillis();
		OpenStackServerProperties.VolumeProperties volumeProperties = this.osProperties.getVolumeWrapper();
		OpenStackServerProperties.ServerProperties serverProperties = this.osProperties.getServerWrapper();
		String serverName = serverProperties.getPrefixName() + currentTimestamp;
		String volumeName = volumeProperties.getPrefixName() + currentTimestamp + "-volume";
		
		// Create volume
		LOGGER.info("{} [serverName {}] Starting creating volume {}", logPrefix, serverName, volumeName);
		VolumeDesc v = VolumeDesc.builder()
				.name(volumeName)
				.bootable(true)
				.description(volumeProperties.getDescription())
				.imageRef(volumeProperties.getImageRef())
				.size(volumeProperties.getSize())
				.volumeType(volumeProperties.getVolumeType())
				.zone(volumeProperties.getZone())
				.build();
		String volumeId = this.volumeService.createVolumeAndBoot(v);

		// Create server and boot on given volume
		LOGGER.info("{} [serverName {}] Starting creating server and booting", logPrefix, serverName);
		ServerDesc s = ServerDesc.builder()
				.name(serverName)
				.keySecurity(serverProperties.getKeySecurity())
				.securityGroup(serverProperties.getSecurityGroup())
				.flavor(serverProperties.getFlavor())
				.availableZone(serverProperties.getAvailableZone())
				.networks(serverProperties.getNetworks())
				.bootVolume(volumeId)
				.bootDeviceName(serverProperties.getBootDeviceName())
				.build();
		String serverId = this.serverService.createAndBootServer(s);

		// Create floating IP
		LOGGER.info("{} [serverName {}] [serverId {}] Starting creating floating ip", logPrefix, serverName, serverId);
		this.serverService.createFloatingIp(serverId, serverProperties.getFloatingNetwork());
		
		return serverId;
	}
}
