package fr.viveris.s1pdgs.scaler.openstack.services;

import java.util.List;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.compute.InterfaceAttachment;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.builder.BlockDeviceMappingBuilder;
import org.openstack4j.model.network.NetFloatingIP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.scaler.openstack.model.ServerDesc;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsServerException;

@Service
public class ServerService {

	private final int serverMaxWaitMs;
	private final int fipMaxLoop;
	private final int fipTempoLoopMs;

	private final OSClientV3 osClient;

	@Autowired
	public ServerService(final OSClientV3 osClient, 
			@Value("${openstack.service.floating-ip.creation.max-loop}") final int fipMaxLoop, 
			@Value("${openstack.service.floating-ip.creation.tempo-loop-ms}") final int fipTempoLoopMs, 
			@Value("${openstack.service.server.creation.max-wait-ms}") final int serverMaxWaitMs) {
		this.osClient = osClient;
		this.fipMaxLoop = fipMaxLoop;
		this.fipTempoLoopMs = fipTempoLoopMs;
		this.serverMaxWaitMs = serverMaxWaitMs;
	}

	public String createAndBootServer(ServerDesc desc) throws OsServerException {
		// Link volume to boot index
		BlockDeviceMappingBuilder blockDeviceMappingBuilder = Builders.blockDeviceMapping().uuid(desc.getBootVolume())
				.deviceName("/dev/vda").bootIndex(0);
		// Create server
		ServerCreate serverCreate = Builders.server().name(desc.getName()).flavor(desc.getFlavor())
				.blockDevice(blockDeviceMappingBuilder.build()).keypairName(desc.getKeySecurity())
				.networks(desc.getNetworks()).availabilityZone(desc.getAvailableZone())
				.addSecurityGroup(desc.getSecurityGroup()).build();
		// Boot server
		Server server = osClient.compute().servers().bootAndWaitActive(serverCreate, serverMaxWaitMs);
		if (server.getStatus() != Server.Status.ACTIVE) {
			throw new OsServerException(String.format("Server not created after %d ms", serverMaxWaitMs));
		}
		return server.getId();
	}

	public void createFloatingIp(String serverId, String floatingNetworkId) throws OsServerException {
		List<? extends InterfaceAttachment> nicID = osClient.compute().servers().interfaces().list(serverId);
		String portid = nicID.get(0).getPortId();
		NetFloatingIP fip = osClient.networking().floatingip()
				.create(Builders.netFloatingIP().floatingNetworkId(floatingNetworkId).portId(portid).build());

		// judge fip is created succeffuly
		int count = 1;
		boolean createFlat = false;
		while (count < fipMaxLoop) {
			if (osClient.networking().floatingip().get(fip.getId()).getStatus().toUpperCase().equals("ACTIVE")) {
				createFlat = true;
				break;
			}
			count++;
			try {
				Thread.sleep(fipTempoLoopMs);
			} catch (InterruptedException e) {
				throw new OsServerException(String.format("[serverId %s] Cannot create floating IP for network %s: %s",
						serverId, floatingNetworkId, e.getMessage()));
			}
		}
		if (!createFlat) {
			throw new OsServerException(String.format("[serverId %s] Floating IP not active after for %d ms", serverId,
					fipMaxLoop * fipTempoLoopMs));
		}
	}

	public void delete(String serverId) {
		osClient.compute().servers().delete(serverId);
	}

}
