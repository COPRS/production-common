package fr.viveris.s1pdgs.scaler.openstack;

import java.util.List;

import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.InterfaceAttachment;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.network.NetFloatingIP;
import org.openstack4j.openstack.OSFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.scaler.openstack.model.ServerDesc;
import fr.viveris.s1pdgs.scaler.openstack.model.ServerDesc.ServerDescBuilder;
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

	private OSClientV3 osClient() {
		OSClientV3 os = OSFactory.builderV3().endpoint(osProperties.getEndpoint())
				.credentials(osProperties.getCredentialUsername(), osProperties.getCredentialPassword(),
						Identifier.byId(osProperties.getDomainId()))
				.scopeToProject(Identifier.byId(osProperties.getProjectId())).authenticate();
		return os;
	}

	@Autowired
	public OpenStackAdministration(final OpenStackServerProperties osProperties, final ServerService serverService,
			final VolumeService volumeService) {
		this.osProperties = osProperties;
		this.serverService = serverService;
		this.volumeService = volumeService;
	}

	public void deleteServer(String serverId) {
		OSClientV3 osClient = this.osClient();
		Server s = this.serverService.get(osClient, serverId);
		OpenStackServerProperties.ServerProperties serverProperties = this.osProperties.getServerWrapper();
		if (serverProperties.isFloatingActivation()) {
			String floatingIP = getFloatingIpForServer(osClient, serverId);
			LOGGER.debug("[serverId {}] Deleting floating ip {}", serverId, floatingIP);
			this.serverService.deleteFloatingIp(osClient, serverId, floatingIP);
		}
		this.serverService.delete(osClient, serverId);
		if (serverProperties.isBootableOnVolume()) {
			for (String v : s.getOsExtendedVolumesAttached()) {
				LOGGER.debug("[serverId {}] Deleting volume {}", serverId, v);
				this.volumeService.deleteVolume(osClient, v);
			}
		}
	}

	public String createServerForL1Wrappers(String logPrefix) throws OsVolumeException, OsServerException {
		OSClientV3 osClient = this.osClient();
		long currentTimestamp = System.currentTimeMillis();
		OpenStackServerProperties.VolumeProperties volumeProperties = this.osProperties.getVolumeWrapper();
		OpenStackServerProperties.ServerProperties serverProperties = this.osProperties.getServerWrapper();
		String serverName = serverProperties.getPrefixName() + currentTimestamp;
		String volumeName = volumeProperties.getPrefixName() + currentTimestamp + "-volume";

		// Create volume
		LOGGER.info("{} [serverName {}] Starting creating volume {}", logPrefix, serverName, volumeName);
		String volumeId = "";
		if (serverProperties.isBootableOnVolume()) {
			VolumeDesc v = VolumeDesc.builder().name(volumeName).bootable(true)
					.description(volumeProperties.getDescription()).imageRef(volumeProperties.getImageRef())
					.size(volumeProperties.getSize()).volumeType(volumeProperties.getVolumeType())
					.zone(volumeProperties.getZone()).build();
			volumeId = this.volumeService.createVolumeAndBoot(osClient, v);
		}

		// Create server and boot on given volume
		LOGGER.info("{} [serverName {}] Starting creating server and booting", logPrefix, serverName);
		ServerDescBuilder builderS = ServerDesc.builder().name(serverName)
				.keySecurity(serverProperties.getKeySecurity()).securityGroups(serverProperties.getSecurityGroups())
				.flavor(serverProperties.getFlavor()).availableZone(serverProperties.getAvailableZone())
				.networks(serverProperties.getNetworks());
		if (serverProperties.isBootableOnVolume()) {
			builderS.bootOnVolumeInformation(volumeId, serverProperties.getBootDeviceName());
		}
		String serverId = this.serverService.createAndBootServer(osClient, builderS.build());

		// Create floating IP
		// TODO boolean in conf
		if (serverProperties.isFloatingActivation()) {
			LOGGER.info("{} [serverName {}] [serverId {}] Starting creating floating ip", logPrefix, serverName,
					serverId);
			this.serverService.createFloatingIp(osClient, serverId, serverProperties.getFloatingNetwork());
		}

		return serverId;
	}
	
	public String getFloatingIpForServer(OSClientV3 osClient, String serverId) {
        List<? extends InterfaceAttachment> nicID = osClient.compute().servers().interfaces().list(serverId);
        String portid = nicID.get(0).getPortId();
        List<? extends NetFloatingIP> fips = osClient.networking().floatingip().list();
        for (NetFloatingIP netFloatingIP : fips) {
               if (netFloatingIP.getPortId().equals(portid)) {
                     return netFloatingIP.getFloatingIpAddress();
               }
        }
        return "";
  }
}
