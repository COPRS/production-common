package esa.s1pdgs.cpoc.scaler.openstack.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.compute.ComputeService;
import org.openstack4j.api.compute.ServerService;
import org.openstack4j.api.compute.ext.InterfaceService;
import org.openstack4j.api.networking.NetFloatingIPService;
import org.openstack4j.api.networking.NetworkingService;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.InterfaceAttachment;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.network.NetFloatingIP;
import org.openstack4j.openstack.compute.domain.NovaServer;
import org.openstack4j.openstack.networking.domain.NeutronFloatingIP;

import esa.s1pdgs.cpoc.scaler.openstack.model.ServerDesc;
import esa.s1pdgs.cpoc.scaler.openstack.model.exceptions.OsEntityException;
import esa.s1pdgs.cpoc.scaler.openstack.model.exceptions.OsFloatingIpNotActiveException;
import esa.s1pdgs.cpoc.scaler.openstack.model.exceptions.OsServerNotActiveException;
import esa.s1pdgs.cpoc.scaler.openstack.model.exceptions.OsServerNotDeletedException;

public class ServerServiceTest {

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private OSClientV3 client;

    @Mock
    private ComputeService computeService;

    @Mock
    private ServerService serverService;

    @Mock
    private InterfaceService interfaceService;

    @Mock
    private NetworkingService networkingService;

    @Mock
    private NetFloatingIPService floatingIpService;

    @Mock
    private List<? extends InterfaceAttachment> nicIds;

    @Mock
    private InterfaceAttachment nicId;

    private esa.s1pdgs.cpoc.scaler.openstack.services.ServerService service;

    private Map<String, Server> servers;
    private Map<String, NetFloatingIP> floatingIps;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        servers = new HashMap<>();
        servers.put("server1", buildServer("s-id-1", "s-name-1"));
        servers.put("server2", buildServer("s-id-2", "s-name-2"));
        floatingIps = new HashMap<>();
        floatingIps.put("float1",
                buildFloatingIp("float1", "f-id-1", "p-id-1"));
        floatingIps.put("floatnull",
                buildFloatingIp("floatnull", "f-id-1", null));
        floatingIps.put("float2",
                buildFloatingIp("float2", "f-id-2", "p-id-2"));
        floatingIps.put("float3",
                buildFloatingIp("float3", "f-id-3", "p-id-3"));
        this.mockOsClient();

        service = new esa.s1pdgs.cpoc.scaler.openstack.services.ServerService(
                3, 0, 0, 500, 1523);
    }

    private Server buildServer(String id, String name) {
        NovaServer server = new NovaServer();
        server.id = id;
        server.name = name;
        server.status = Server.Status.ACTIVE;
        return server;
    }

    private Server buildServerFailed(String id, String name) {
        NovaServer server = new NovaServer();
        server.id = id;
        server.name = name;
        server.status = Server.Status.ERROR;
        return server;
    }

    private NetFloatingIP buildFloatingIp(String id, String networkId,
            String portId) {
        NeutronFloatingIP ip = new NeutronFloatingIP();
        ip.setId(id);
        ip.setFloatingNetworkId(networkId);
        ip.setPortId(portId);
        ip.setStatus("ACTIVE");
        return ip;
    }

    private NetFloatingIP buildFloatingIpFailed(String id, String networkId,
            String portId) {
        NeutronFloatingIP ip = new NeutronFloatingIP();
        ip.setId(id);
        ip.setFloatingNetworkId(networkId);
        ip.setPortId(portId);
        ip.setStatus("ERROR");
        return ip;
    }

    private void mockOsClient() {
        doReturn(computeService).when(client).compute();
        doReturn(serverService).when(computeService).servers();
        doReturn(networkingService).when(client).networking();
        doReturn(floatingIpService).when(networkingService).floatingip();
        doReturn(interfaceService).when(serverService).interfaces();

        doReturn(Arrays.asList(servers.get("server1"), servers.get("server2")))
                .when(serverService).list();
        doReturn(Arrays.asList(servers.get("server1"), servers.get("server2")))
                .when(serverService).list(Mockito.any(Map.class));
        doAnswer(i -> {
            String serverId = (String) i.getArgument(0);
            return servers.get(serverId);
        }).when(serverService).get(Mockito.anyString());
        doReturn(ActionResponse.actionSuccess()).when(serverService)
                .delete(Mockito.eq("server1"));
        doReturn(ActionResponse.actionFailed("error", -1)).when(serverService)
                .delete(Mockito.eq("server2"));

        doReturn(servers.get("server1")).when(serverService)
                .bootAndWaitActive(Mockito.any(), Mockito.anyInt());

        doReturn(nicIds).when(interfaceService).list(Mockito.anyString());
        doReturn(nicId).when(nicIds).get(Mockito.anyInt());
        doReturn("p-id-2").when(nicId).getPortId();

        doReturn(Arrays.asList(floatingIps.get("float1"),
                floatingIps.get("floatnull"), floatingIps.get("float2"),
                floatingIps.get("float3"))).when(floatingIpService).list();
        doReturn(null).when(floatingIpService).delete(Mockito.anyString());
        doReturn(floatingIps.get("float1")).when(floatingIpService)
                .create(Mockito.any());
        doAnswer(i -> {
            String fid = (String) i.getArgument(0);
            return floatingIps.get(fid);
        }).when(floatingIpService).get(Mockito.anyString());

    }

    @Test
    public void testCreateFloatingIpWhenNoActive() throws OsEntityException {
        doReturn(buildFloatingIpFailed("float2", "f-id-2", "p-id-2"))
                .when(floatingIpService).create(Mockito.any());
        doReturn(buildFloatingIpFailed("float2", "f-id-2", "p-id-2"))
                .when(floatingIpService).get(Mockito.anyString());

        thrown.expect(OsFloatingIpNotActiveException.class);
        thrown.expect(hasProperty("identifier", is("server2")));
        service.createFloatingIp(client, "server2", "float2");
    }

    @Test
    public void testCreateFloatingIp() throws OsEntityException {
        
        service.createFloatingIp(client, "server2", "float2");
        
        ArgumentCaptor<NetFloatingIP> argument =
                ArgumentCaptor.forClass(NetFloatingIP.class);
        verify(floatingIpService, times(1)).create(argument.capture());
        assertEquals("p-id-2", argument.getValue().getPortId());
        assertEquals("float2", argument.getValue().getFloatingNetworkId());
        
    }

    @Test
    public void testCreateAndBootServerWhenNotActive()
            throws OsServerNotActiveException {
        ServerDesc desc = ServerDesc.builder().availableZone("zone")
                .bootOnVolumeInformation("boot-volume", "boot-device-name")
                .flavor("flavor").identifier("id").imageRef("image-ref")
                .keySecurity("key-security").name("s-name-1")
                .networks(Arrays.asList("network1", "network2"))
                .securityGroups(Arrays.asList("group1", "group2")).build();

        doReturn(buildServerFailed("s-id-1", "s-name-1")).when(serverService)
                .bootAndWaitActive(Mockito.any(), Mockito.anyInt());

        thrown.expect(OsServerNotActiveException.class);
        thrown.expect(hasProperty("identifier", is("s-id-1")));

        service.createAndBootServer(client, desc);
    }

    @Test
    public void testCreateAndBootServer() throws OsServerNotActiveException {
        ServerDesc desc = ServerDesc.builder().availableZone("zone")
                .bootOnVolumeInformation("boot-volume", "boot-device-name")
                .flavor("flavor").identifier("id").imageRef("image-ref")
                .keySecurity("key-security").name("s-name-1")
                .networks(Arrays.asList("network1", "network2"))
                .securityGroups(Arrays.asList("group1", "group2")).build();

        ArgumentCaptor<ServerCreate> argument =
                ArgumentCaptor.forClass(ServerCreate.class);
        String serverId = service.createAndBootServer(client, desc);

        assertEquals("s-id-1", serverId);
        verify(serverService, times(1)).bootAndWaitActive(argument.capture(),
                Mockito.eq(1523));

        assertEquals("zone", argument.getValue().getAvailabilityZone());
        assertEquals("s-name-1", argument.getValue().getName());
        assertEquals("flavor", argument.getValue().getFlavorRef());
        assertEquals("key-security", argument.getValue().getKeyName());
        assertNull(argument.getValue().getImageRef());
        assertEquals(2, argument.getValue().getNetworks().size());
        assertEquals("network1",
                argument.getValue().getNetworks().get(0).getId());
        assertEquals(2, argument.getValue().getSecurityGroups().size());
        assertEquals("group1",
                argument.getValue().getSecurityGroups().get(0).getName());
    }

    @Test
    public void testCreateAndBootServerNoSecGroup()
            throws OsServerNotActiveException {
        ServerDesc desc = ServerDesc.builder().availableZone("zone")
                .bootOnVolumeInformation("boot-volume", "boot-device-name")
                .flavor("flavor").identifier("id").imageRef("image-ref")
                .keySecurity("key-security").name("s-name-1")
                .networks(Arrays.asList("network1", "network2")).build();

        ArgumentCaptor<ServerCreate> argument =
                ArgumentCaptor.forClass(ServerCreate.class);
        String serverId = service.createAndBootServer(client, desc);

        assertEquals("s-id-1", serverId);
        verify(serverService, times(1)).bootAndWaitActive(argument.capture(),
                Mockito.eq(1523));

        assertEquals("zone", argument.getValue().getAvailabilityZone());
        assertEquals("s-name-1", argument.getValue().getName());
        assertEquals("flavor", argument.getValue().getFlavorRef());
        assertEquals("key-security", argument.getValue().getKeyName());
        assertNull(argument.getValue().getImageRef());
        assertEquals(2, argument.getValue().getNetworks().size());
        assertEquals("network1",
                argument.getValue().getNetworks().get(0).getId());
        assertNull(argument.getValue().getSecurityGroups());
    }

    @Test
    public void testCreateAndBootServerNoVolume()
            throws OsServerNotActiveException {
        ServerDesc desc = ServerDesc.builder().availableZone("zone")
                .flavor("flavor").identifier("id").imageRef("image-ref")
                .keySecurity("key-security").name("s-name-1")
                .networks(Arrays.asList("network1", "network2"))
                .securityGroups(Arrays.asList("group1", "group2")).build();

        ArgumentCaptor<ServerCreate> argument =
                ArgumentCaptor.forClass(ServerCreate.class);
        String serverId = service.createAndBootServer(client, desc);

        assertEquals("s-id-1", serverId);
        verify(serverService, times(1)).bootAndWaitActive(argument.capture(),
                Mockito.eq(1523));

        assertEquals("zone", argument.getValue().getAvailabilityZone());
        assertEquals("s-name-1", argument.getValue().getName());
        assertEquals("flavor", argument.getValue().getFlavorRef());
        assertEquals("key-security", argument.getValue().getKeyName());
        assertEquals("image-ref", argument.getValue().getImageRef());
        assertEquals(2, argument.getValue().getNetworks().size());
        assertEquals("network1",
                argument.getValue().getNetworks().get(0).getId());
        assertEquals(2, argument.getValue().getSecurityGroups().size());
        assertEquals("group1",
                argument.getValue().getSecurityGroups().get(0).getName());
    }

    @Test
    public void testDeleteFloatingIp() {
        service.deleteFloatingIp(client, "f-id-1");

        verify(floatingIpService, times(1)).delete(Mockito.eq("f-id-1"));
    }

    @Test(expected = OsServerNotDeletedException.class)
    public void testDeleteResponseKoStatusFailed() throws OsEntityException {
        service = new esa.s1pdgs.cpoc.scaler.openstack.services.ServerService(
                0, 2, 500, 0, 0);
        service.delete(client, "server2");
    }

    @Test(expected = OsServerNotDeletedException.class)
    public void testDeleteResponseOkStatusFailed() throws OsEntityException {
        service = new esa.s1pdgs.cpoc.scaler.openstack.services.ServerService(
                0, 2, 1000, 0, 0);
        service.delete(client, "server1");
    }

    @Test
    public void testDeleteResponseOkStatusOk() throws OsEntityException {
        long before = System.currentTimeMillis();
        doReturn(servers.get("server1"), null).when(serverService)
                .get(Mockito.eq("server1"));
        service = new esa.s1pdgs.cpoc.scaler.openstack.services.ServerService(
                0, 2, 1000, 0, 0);
        service.delete(client, "server1");
        long after = System.currentTimeMillis();

        verify(serverService, times(1)).delete(Mockito.eq("server1"));
        verify(serverService, times(2)).get(Mockito.eq("server1"));
        // Only one loop
        assertTrue(before + 1000 <= after);
        assertTrue(before + 2000 > after);
    }

    @Test
    public void testGetServer() {
        Server s1 = service.get(client, "server1");
        assertEquals(servers.get("server1"), s1);
        Server s2 = service.get(client, "server2");
        assertEquals(servers.get("server2"), s2);
    }

    @Test
    public void tesGetFloatingIp() {
        String floatId = service.getFloatingIpIdForServer(client, "server1");
        assertEquals("float2", floatId);
    }

    @Test
    public void tesGetFloatingIpWhenNoIps() {
        doReturn(new ArrayList<>()).when(floatingIpService).list();
        String floatId = service.getFloatingIpIdForServer(client, "server1");
        assertEquals("", floatId);
    }

    @Test
    public void testGetServerIds() {
        Map<String, String> expFilter = new HashMap<String, String>();
        expFilter.put("status", "status-test");
        expFilter.put("name", "^prefix-test*");
        Map<String, String> expServer = new HashMap<String, String>();
        expServer.put("s-id-1", "s-name-1");
        expServer.put("s-id-2", "s-name-2");

        Map<String, String> result =
                service.getServerIds(client, "prefix-test", "status-test");

        assertEquals(expServer, result);
        verify(serverService, times(1)).list(Mockito.eq(expFilter));
    }

}
