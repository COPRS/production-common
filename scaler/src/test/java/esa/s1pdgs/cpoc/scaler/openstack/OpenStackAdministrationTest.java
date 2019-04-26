package esa.s1pdgs.cpoc.scaler.openstack;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.openstack.compute.domain.NovaServer;

import esa.s1pdgs.cpoc.common.errors.os.OsEntityException;
import esa.s1pdgs.cpoc.scaler.openstack.model.ServerDesc;
import esa.s1pdgs.cpoc.scaler.openstack.model.ServerDesc.ServerDescBuilder;
import esa.s1pdgs.cpoc.scaler.openstack.model.VolumeDesc;
import esa.s1pdgs.cpoc.scaler.openstack.services.ServerService;
import esa.s1pdgs.cpoc.scaler.openstack.services.VolumeService;
import test.MockPropertiesTest;

public class OpenStackAdministrationTest extends MockPropertiesTest {

    /**
     * Service for managing servers
     */
    @Mock
    private OpenStackClientFactory osClientFactory;

    /**
     * OS client
     */
    @Mock
    private OSClientV3 osClient;

    /**
     * Service for managing servers
     */
    @Mock
    private ServerService serverService;

    /**
     * 
     */
    @Mock
    private NovaServer server;

    /**
     * Service for managing volumes
     */
    @Mock
    private VolumeService volumeService;

    /**
     * Administration service to test
     */
    private OpenStackAdministration osAdmin;

    /**
     * Initialization
     * 
     * @throws OsEntityException
     */
    @Before
    public void init() throws OsEntityException {
        // Init mock and properties
        super.initTest();

        // Mock OS client
        doReturn(osClient).when(osClientFactory).osClient(Mockito.any());

        // Mock server
        doReturn(server).when(serverService).get(Mockito.any(),
                Mockito.anyString());
        List<String> volumes = new ArrayList<>();
        volumes.add("vol1");
        volumes.add("vol2");
        doReturn(server).when(serverService).get(Mockito.any(),
                Mockito.anyString());
        doReturn(volumes).when(server).getOsExtendedVolumesAttached();
        doReturn("float-ip").when(serverService)
                .getFloatingIpIdForServer(Mockito.any(), Mockito.anyString());
        doReturn("server-id").when(serverService)
                .createAndBootServer(Mockito.any(), Mockito.any());
        doNothing().when(serverService).createFloatingIp(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());

        // Mock volume service
        doReturn("vol-id").when(volumeService)
                .createVolumeAndBoot(Mockito.any(), Mockito.any());

        // Mock tested object
        osAdmin = new OpenStackAdministration(osClientFactory, osProperties,
                serverService, volumeService);
    }

    /**
     * @throws OsEntityException
     */
    @Test
    public void testDeleteServerWhenAll() throws OsEntityException {
        osAdmin.deleteServer("server-id");

        verify(serverService, times(1)).get(Mockito.any(),
                Mockito.eq("server-id"));
        verify(serverService, times(1)).getFloatingIpIdForServer(Mockito.any(),
                Mockito.eq("server-id"));
        verify(serverService, times(1)).deleteFloatingIp(Mockito.any(),
                Mockito.eq("float-ip"));
        verify(serverService, times(1)).delete(Mockito.any(),
                Mockito.eq("server-id"));
        verify(volumeService, times(1)).deleteVolume(Mockito.any(),
                Mockito.eq("vol1"));
        verify(volumeService, times(1)).deleteVolume(Mockito.any(),
                Mockito.eq("vol2"));
    }

    /**
     * @throws OsEntityException
     */
    @Test
    public void testDeleteServerWhenNoVolume() throws OsEntityException {
        doReturn(false).when(osServerProperties).isBootableOnVolume();
        osAdmin.deleteServer("server-id");

        verify(serverService, times(1)).get(Mockito.any(),
                Mockito.eq("server-id"));
        verify(serverService, times(1)).getFloatingIpIdForServer(Mockito.any(),
                Mockito.eq("server-id"));
        verify(serverService, times(1)).deleteFloatingIp(Mockito.any(),
                Mockito.eq("float-ip"));
        verify(serverService, times(1)).delete(Mockito.any(),
                Mockito.eq("server-id"));
        verify(volumeService, never()).deleteVolume(Mockito.any(),
                Mockito.anyString());
    }

    /**
     * @throws OsEntityException
     */
    @Test
    public void testDeleteServerWhenNoFloat() throws OsEntityException {
        doReturn(false).when(osServerProperties).isFloatActivation();
        osAdmin.deleteServer("server-id");

        verify(serverService, times(1)).get(Mockito.any(),
                Mockito.eq("server-id"));
        verify(serverService, never()).getFloatingIpIdForServer(Mockito.any(),
                Mockito.anyString());
        verify(serverService, never()).deleteFloatingIp(Mockito.any(),
                Mockito.anyString());
        verify(serverService, times(1)).delete(Mockito.any(),
                Mockito.eq("server-id"));
        verify(volumeService, times(1)).deleteVolume(Mockito.any(),
                Mockito.eq("vol1"));
        verify(volumeService, times(1)).deleteVolume(Mockito.any(),
                Mockito.eq("vol2"));
    }

    @Test
    public void testInternalCreateServerWhenAll() throws OsEntityException {
        osAdmin.createServerForL1Wrappers("log-prefix", "vm-id");

        ServerDesc expectedServer = getExpectedServer(
                osServerPropertiesExp.getPrefixName() + "-vm-id", "vol-id",
                true);
        VolumeDesc expectedVolume = getExpectedVolume(
                osVolumePropertiesExp.getPrefixName() + "-vm-id-volume");

        verify(volumeService, times(1)).createVolumeAndBoot(Mockito.any(),
                Mockito.eq(expectedVolume));
        verify(serverService, times(1)).createAndBootServer(Mockito.any(),
                Mockito.eq(expectedServer));
        verify(serverService, times(1)).createFloatingIp(Mockito.any(),
                Mockito.eq("server-id"),
                Mockito.eq(osServerPropertiesExp.getFloatingNetwork()));
    }

    @Test
    public void testInternalCreateServerWhenNoVol() throws OsEntityException {
        doReturn(false).when(osServerProperties).isBootableOnVolume();
        osAdmin.createServerForL1Wrappers("log-prefix", "vm-id");

        ServerDesc expectedServer = getExpectedServer(
                osServerPropertiesExp.getPrefixName() + "-vm-id", "vol-id",
                false);

        verify(volumeService, never()).createVolumeAndBoot(Mockito.any(),
                Mockito.any());
        verify(serverService, times(1)).createAndBootServer(Mockito.any(),
                Mockito.eq(expectedServer));
        verify(serverService, times(1)).createFloatingIp(Mockito.any(),
                Mockito.eq("server-id"),
                Mockito.eq(osServerPropertiesExp.getFloatingNetwork()));
    }

    @Test
    public void testInternalCreateServerWhenNoFloat() throws OsEntityException {
        doReturn(false).when(osServerProperties).isFloatActivation();
        osAdmin.createServerForL1Wrappers("log-prefix", "vm-id");

        ServerDesc expectedServer = getExpectedServer(
                osServerPropertiesExp.getPrefixName() + "-vm-id", "vol-id",
                true);
        VolumeDesc expectedVolume = getExpectedVolume(
                osVolumePropertiesExp.getPrefixName() + "-vm-id-volume");

        verify(volumeService, times(1)).createVolumeAndBoot(Mockito.any(),
                Mockito.eq(expectedVolume));
        verify(serverService, times(1)).createAndBootServer(Mockito.any(),
                Mockito.eq(expectedServer));
        verify(serverService, never()).createFloatingIp(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testCreateServerWhenAll() throws OsEntityException {
        AtomicInteger number = new AtomicInteger();
        osAdmin.createServerForL1Wrappers("log-prefix", number);

        verify(volumeService, times(1)).createVolumeAndBoot(Mockito.any(),
                Mockito.any());
        verify(serverService, times(1)).createAndBootServer(Mockito.any(),
                Mockito.any());
        verify(serverService, times(1)).createFloatingIp(Mockito.any(),
                Mockito.eq("server-id"),
                Mockito.eq(osServerPropertiesExp.getFloatingNetwork()));
    }

    private ServerDesc getExpectedServer(String serverName, String volumeId,
            boolean volume) {
        ServerDescBuilder builderS = ServerDesc.builder().name(serverName)
                .keySecurity(osServerPropertiesExp.getKeySecurity())
                .securityGroups(osServerPropertiesExp.getSecurityGroups())
                .flavor(osServerPropertiesExp.getFlavor())
                .availableZone(osServerPropertiesExp.getAvailableZone())
                .networks(osServerPropertiesExp.getNetworks());
        if (volume) {
            builderS.bootOnVolumeInformation(volumeId,
                    osServerPropertiesExp.getBootDeviceName());
        } else {
            builderS.imageRef(osServerProperties.getImageRef());
        }
        return builderS.build();
    }

    private VolumeDesc getExpectedVolume(String volumeName) {
        return VolumeDesc.builder().name(volumeName).bootable(true)
                .description(osVolumePropertiesExp.getDescription())
                .imageRef(osServerPropertiesExp.getImageRef())
                .size(osVolumePropertiesExp.getSize())
                .volumeType(osVolumePropertiesExp.getVolumeType())
                .zone(osVolumePropertiesExp.getZone()).build();
    }

    @Test
    public void testDeleteInvalidVolume() throws OsEntityException {
        Map<String, String> invalidVolumes = new HashMap<>();
        invalidVolumes.put("volume1", "name1");
        invalidVolumes.put("volume2", "name2");
        doReturn(invalidVolumes).when(volumeService).getVolumeIds(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());

        osAdmin.deleteInvalidVolumes();

        verify(volumeService, times(1)).getVolumeIds(Mockito.any(),
                Mockito.eq(osVolumePropertiesExp.getPrefixName()),
                Mockito.eq("ERROR"));
        verify(volumeService, times(2)).deleteVolume(Mockito.any(),
                Mockito.anyString());
        verify(volumeService, times(1)).deleteVolume(Mockito.any(),
                Mockito.eq("volume1"));
        verify(volumeService, times(1)).deleteVolume(Mockito.any(),
                Mockito.eq("volume2"));
    }

    @Test
    public void testDeleteInvalidServer() throws OsEntityException {
        Map<String, String> invalidServers = new HashMap<>();
        invalidServers.put("server1", "name1");
        invalidServers.put("server2", "name2");
        doReturn(invalidServers).when(serverService).getServerIds(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());

        osAdmin.deleteInvalidServers();

        verify(serverService, times(1)).getServerIds(Mockito.any(),
                Mockito.eq(osServerPropertiesExp.getPrefixName()),
                Mockito.eq("ERROR"));
        verify(serverService, times(2)).delete(Mockito.any(),
                Mockito.anyString());
        verify(serverService, times(1)).delete(Mockito.any(),
                Mockito.eq("server1"));
        verify(serverService, times(1)).delete(Mockito.any(),
                Mockito.eq("server2"));
    }
}
