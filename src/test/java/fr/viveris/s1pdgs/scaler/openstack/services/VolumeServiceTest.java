package fr.viveris.s1pdgs.scaler.openstack.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.storage.BlockStorageService;
import org.openstack4j.api.storage.BlockVolumeService;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.storage.block.Volume;

import fr.viveris.s1pdgs.scaler.openstack.model.VolumeDesc;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsEntityException;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsVolumeNotAvailableException;

public class VolumeServiceTest {

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private OSClientV3 client;

    @Mock
    private BlockStorageService blockStorageService;

    @Mock
    private BlockVolumeService blockVolumeService;

    @Mock
    private Volume volume1;

    @Mock
    private Volume volume2;

    private VolumeService service;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mockOsClient();

        service = new VolumeService(4, 500);
    }

    private void mockOsClient() {
        doReturn("volume1").when(volume1).getId();
        doReturn("volume1-name").when(volume1).getName();
        doReturn(Volume.Status.AVAILABLE).when(volume1).getStatus();

        doReturn("volume2").when(volume2).getId();
        doReturn("volume2-name").when(volume2).getName();
        doReturn(Volume.Status.AVAILABLE).when(volume2).getStatus();

        doReturn(blockStorageService).when(client).blockStorage();
        doReturn(blockVolumeService).when(blockStorageService).volumes();
        doReturn(Arrays.asList(volume1, volume2)).when(blockVolumeService)
                .list(Mockito.any(Map.class));

        doAnswer(i -> {
            String serverId = (String) i.getArgument(0);
            if (serverId.equals("volume1")) {
                return volume1;
            }
            return volume2;
        }).when(blockVolumeService).get(Mockito.anyString());

        doReturn(ActionResponse.actionSuccess()).when(blockVolumeService)
                .delete(Mockito.eq("volume1"));
        doReturn(ActionResponse.actionFailed("error", -1))
                .when(blockVolumeService).delete(Mockito.eq("volume2"));

        doReturn(volume1).when(blockVolumeService).create(Mockito.any());
    }

    @Test
    public void testCreateAndBootWhenNotActive() throws OsEntityException {
        VolumeDesc desc =
                VolumeDesc.builder().bootable(true).description("desc")
                        .id("idd").imageRef("image-ref").name("namee").size(40)
                        .volumeType("type").zone("zonee").build();
        doReturn(Volume.Status.ERROR, Volume.Status.ERROR, Volume.Status.ERROR,
                Volume.Status.AVAILABLE).when(volume1).getStatus();

        thrown.expect(OsVolumeNotAvailableException.class);
        thrown.expect(hasProperty("identifier", is("namee")));

        service.createVolumeAndBoot(client, desc);
    }

    @Test
    public void testCreateAndBoot() throws OsEntityException {
        VolumeDesc desc =
                VolumeDesc.builder().bootable(true).description("desc")
                        .id("idd").imageRef("image-ref").name("namee").size(40)
                        .volumeType("type").zone("zonee").build();
        doReturn(Volume.Status.ERROR, Volume.Status.ERROR,
                Volume.Status.AVAILABLE).when(volume1).getStatus();

        long before = System.currentTimeMillis();
        String volumeId = service.createVolumeAndBoot(client, desc);
        long after = System.currentTimeMillis();
        
        assertEquals("volume1", volumeId);
        assertTrue(before + 1000 <= after);
        assertTrue(before + 1500 >= after);
    }

    @Test
    public void testGetVolumeIds() {
        Map<String, String> expFilter = new HashMap<String, String>();
        expFilter.put("status", "status-test");
        expFilter.put("name", "^prefix-test*");
        Map<String, String> expServer = new HashMap<String, String>();
        expServer.put("volume1", "volume1-name");
        expServer.put("volume2", "volume2-name");

        Map<String, String> result =
                service.getVolumeIds(client, "prefix-test", "status-test");

        assertEquals(expServer, result);
        verify(blockVolumeService, times(1)).list(Mockito.eq(expFilter));
    }

    @Test
    public void testDelete() {
        service.deleteVolume(client, "volume1");
        verify(blockVolumeService, times(1)).delete(Mockito.eq("volume1"));
    }
}
