package esa.s1pdgs.cpoc.archives.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.archives.DevProperties;
import esa.s1pdgs.cpoc.archives.services.ObsService;
import esa.s1pdgs.cpoc.archives.status.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

public class SlicesConsumerTest {

    @Mock
    private ObsService obsService;
    
    @Mock
    private DevProperties devProperties;
    

    @Mock
    private AppStatus appStatus;
    
    /**
     * Acknowledgement
     */
    @Mock
    private Acknowledgment ack;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockDevProperties(false);
    }
    
    private void mockDevProperties(boolean act) {
        Map<String, Boolean> activations = new HashMap<>();
        activations.put("download-all", act);
        doReturn(activations).when(devProperties).getActivations();
    }

    private void mockSliceDownloadFiles(File result)
            throws ObsException, ObsUnknownObject {
        doReturn(result).when(obsService).downloadFile(
                Mockito.any(ProductFamily.class), Mockito.anyString(),
                Mockito.anyString());
    }

    private void mockSliceObjectStorageException()
            throws ObsException, ObsUnknownObject {
        doThrow(new ObsException(ProductFamily.L0_SLICE, "kobs",
                new Throwable())).when(obsService).downloadFile(
                        Mockito.any(ProductFamily.class), Mockito.anyString(),
                        Mockito.anyString());
    }

    private void mockSliceObsUnknownObjectException()
            throws ObsUnknownObject, ObsException {
        doThrow(new ObsUnknownObject(ProductFamily.BLANK, "kobs"))
                .when(obsService).downloadFile(Mockito.any(ProductFamily.class),
                        Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testReceiveL0Slice()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        File expectedResult =
                new File("test/data/slices/l0_slice/productName");
        mockDevProperties(true);
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsService, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L0_SLICE), Mockito.eq("kobs"),
                Mockito.eq("test/data/slices/l0_slice"));
    }

    @Test
    public void testReceiveL0SliceOnlyManifest()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        File expectedResult =
                new File("test/data/slices/l0_slice/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsService, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L0_SLICE), Mockito.eq("kobs/manifest.safe"),
                Mockito.eq("test/data/slices/l0_slice"));
    }

    @Test
    public void testReceiveL0SliceObjectStorageException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        this.mockSliceObjectStorageException();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }

    @Test
    public void testReceiveL0SliceObsUnknownObjectException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        this.mockSliceObsUnknownObjectException();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }
    
    @Test
    public void testReceiveL0SliceAckException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        File expectedResult =
                new File("test/data/slices/l0_slice/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

    @Test
    public void testReceiveL1Slice()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        File expectedResult =
                new File("test/data/slices/l1_slice/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L1_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsService, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L1_SLICE), Mockito.eq("kobs/manifest.safe"),
                Mockito.eq("test/data/slices/l1_slice"));
    }

    @Test
    public void testReceiveL1SliceObjectStorageException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        this.mockSliceObjectStorageException();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L1_SLICE, "NRT"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }

    @Test
    public void testReceiveL1SliceObsUnknownObjectException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        this.mockSliceObsUnknownObjectException();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L1_SLICE, "NRT"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }
    
    @Test
    public void testReceiveL1SliceAckException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        File expectedResult =
                new File("test/data/slices/l1_slice/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.L1_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

    @Test
    public void testReceiveUnknownSlice() throws ObsException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties,
                        appStatus);
        consumer.receive(
                new LevelProductDto("productName", "kobs", ProductFamily.BLANK, "NRT"),
                ack, "topic");
    }

}
