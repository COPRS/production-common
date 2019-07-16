package esa.s1pdgs.cpoc.scaler.openstack.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.InterfaceAttachment;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.builder.BlockDeviceMappingBuilder;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;
import org.openstack4j.model.network.NetFloatingIP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.common.errors.os.OsEntityException;
import esa.s1pdgs.cpoc.common.errors.os.OsEntityInternaloErrorException;
import esa.s1pdgs.cpoc.common.errors.os.OsFloatingIpNotActiveException;
import esa.s1pdgs.cpoc.common.errors.os.OsServerNotActiveException;
import esa.s1pdgs.cpoc.common.errors.os.OsServerNotDeletedException;
import esa.s1pdgs.cpoc.scaler.openstack.model.ServerDesc;

@Service
public class ServerService {

    private static final Logger LOGGER =
            LogManager.getLogger(ServerService.class);
    private final int serverMaxWaitMs;
    final int deletionMaxLoop;
    final int deletionTempoLoopMs;
    private final int fipMaxLoop;
    private final int fipTempoLoopMs;

    @Autowired
    public ServerService(
            @Value("${openstack.service.floating-ip.creation.max-loop}") final int fipMaxLoop,
            @Value("${openstack.service.server.deletion.max-loop}") final int deletionMaxLoop,
            @Value("${openstack.service.server.deletion.tempo-loop-ms}") final int deletionTempoLoopMs,
            @Value("${openstack.service.floating-ip.creation.tempo-loop-ms}") final int fipTempoLoopMs,
            @Value("${openstack.service.server.creation.max-wait-ms}") final int serverMaxWaitMs) {
        this.fipMaxLoop = fipMaxLoop;
        this.deletionMaxLoop = deletionMaxLoop;
        this.deletionTempoLoopMs = deletionTempoLoopMs;
        this.fipTempoLoopMs = fipTempoLoopMs;
        this.serverMaxWaitMs = serverMaxWaitMs;
    }

    public String createAndBootServer(OSClientV3 osClient, ServerDesc desc)
            throws OsServerNotActiveException {

        ServerCreateBuilder builder = Builders.server().name(desc.getName())
                .flavor(desc.getFlavor()).keypairName(desc.getKeySecurity())
                .networks(desc.getNetworks())
                .availabilityZone(desc.getAvailableZone());
        if (!CollectionUtils.isEmpty(desc.getSecurityGroups())) {
            for (String g : desc.getSecurityGroups()) {
                builder = builder.addSecurityGroup(g);
            }
        }
        if (desc.isBootableOnVolume()) {
            // Link volume to boot index
            BlockDeviceMappingBuilder blockDeviceMappingBuilder =
                    Builders.blockDeviceMapping().uuid(desc.getBootVolume())
                            .deviceName(desc.getBootDeviceName()).bootIndex(0);
            builder = builder.blockDevice(blockDeviceMappingBuilder.build());
        } else {
            // Add image ref for server
            builder.image(desc.getImageRef());
        }

        // Create server
        ServerCreate serverCreate = builder.build();
        Server server = osClient.compute().servers()
                .bootAndWaitActive(serverCreate, serverMaxWaitMs);
        if (server.getStatus() != Server.Status.ACTIVE) {
            throw new OsServerNotActiveException(server.getId(), String
                    .format("Server not created after %d ms", serverMaxWaitMs));
        }
        return server.getId();
    }

    public void createFloatingIp(OSClientV3 osClient, String serverId,
            String floatingNetworkId) throws OsEntityException {
        List<? extends InterfaceAttachment> nicID =
                osClient.compute().servers().interfaces().list(serverId);
        String portid = nicID.get(0).getPortId();
        NetFloatingIP fip = osClient.networking().floatingip()
                .create(Builders.netFloatingIP()
                        .floatingNetworkId(floatingNetworkId).portId(portid)
                        .build());

        // judge fip is created succeffuly
        int count = 1;
        boolean createFlat = false;
        while (count < fipMaxLoop) {
            if (osClient.networking().floatingip().get(fip.getId()).getStatus()
                    .toUpperCase().equals("ACTIVE")) {
                createFlat = true;
                break;
            }
            count++;
            try {
                Thread.sleep(fipTempoLoopMs);
            } catch (InterruptedException e) {
                throw new OsEntityInternaloErrorException("serverId", serverId,
                        String.format(
                                "Cannot create floating IP for network %s: %s",
                                floatingNetworkId, e.getMessage()),
                        e);
            }
        }
        if (!createFlat) {
            throw new OsFloatingIpNotActiveException(serverId,
                    String.format("Floating IP not active after for %d ms",
                            fipMaxLoop * fipTempoLoopMs));
        }
    }

    public void deleteFloatingIp(OSClientV3 osClient, String floatingIpId) {
        osClient.networking().floatingip().delete(floatingIpId);
    }

    public void delete(OSClientV3 osClient, String serverId)
            throws OsEntityException {
        ActionResponse deleteRespone =
                osClient.compute().servers().delete(serverId);
        if (deleteRespone.isSuccess()) {
            LOGGER.debug("[serverId {}] Server is deleted", serverId);
        }
        boolean deleteServerStatus = false;
        for (int i = 0; i < deletionMaxLoop; i++) {
            if (osClient.compute().servers().get(serverId) == null) {
                deleteServerStatus = true;
                LOGGER.debug("[serverId {}] Server is deleted", serverId);
                break;
            }
            try {
                Thread.sleep(deletionTempoLoopMs);
            } catch (InterruptedException e) {
                throw new OsEntityInternaloErrorException("serverId", serverId,
                        String.format("Fail to sleep: %s", e.getMessage()), e);
            }
        }
        if (!deleteServerStatus) {
            throw new OsServerNotDeletedException(serverId,
                    "Fail to delete server");
        }

    }

    public Server get(OSClientV3 osClient, String serverId) {
    	org.openstack4j.api.compute.ServerService service = osClient.compute().servers();
    	for (Server server: service.list()) {
    		LOGGER.debug("list server: {} {} {}",server.getName(), server.getHost(), server.getId(), server.getUuid());
    		LOGGER.debug(server);
    	}
    	
        return osClient.compute().servers().get(serverId);
    }

    public String getFloatingIpIdForServer(final OSClientV3 osClient,
            final String serverId) {
        List<? extends InterfaceAttachment> nicID =
                osClient.compute().servers().interfaces().list(serverId);
        if (!CollectionUtils.isEmpty(nicID)) {
            String portid = nicID.get(0).getPortId();
            List<? extends NetFloatingIP> fips =
                    osClient.networking().floatingip().list();
            for (NetFloatingIP netFloatingIP : fips) {
                if (netFloatingIP.getPortId() != null
                        && netFloatingIP.getPortId().equals(portid)) {
                    return netFloatingIP.getId();
                }
            }
        }
        return "";
    }

    public Map<String, String> getServerIds(final OSClientV3 osClient,
            final String prefix, final String status) {
        Map<String, String> ret = new HashMap<String, String>();
        Map<String, String> filter = new HashMap<String, String>();
        filter.put("status", status);
        filter.put("name", "^" + prefix + "*");
        for (Server server : osClient.compute().servers().list(filter)) {
            ret.put(server.getId(), server.getName());
        }
        return ret;
    }

}
