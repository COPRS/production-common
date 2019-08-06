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
import esa.s1pdgs.cpoc.archives.status.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class SegmentsConsumerTest {

    @Mock
    private ObsClient obsClient;
    
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
        doReturn(result).when(obsClient).downloadFile(
                Mockito.any(ProductFamily.class), Mockito.anyString(),
                Mockito.anyString());
    }

    private void mockSliceObjectStorageException()
            throws ObsException, ObsUnknownObject {
        doThrow(new ObsException(ProductFamily.L0_SEGMENT, "kobs",
                new Throwable())).when(obsClient).downloadFile(
                        Mockito.any(ProductFamily.class), Mockito.anyString(),
                        Mockito.anyString());
    }

    private void mockSliceObsUnknownObjectException()
            throws ObsUnknownObject, ObsException {
        doThrow(new ObsUnknownObject(ProductFamily.BLANK, "kobs"))
                .when(obsClient).downloadFile(Mockito.any(ProductFamily.class),
                        Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testReceiveL0Segment()
            throws ObsException, ObsUnknownObject {
        SegmentsConsumer consumer =
                new SegmentsConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        File expectedResult =
                new File("test/data/segments/l0_segment/productName");
        mockDevProperties(true);
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductDto("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsClient, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq("kobs"),
                Mockito.eq("test/data/segments/l0_segment"));
    }

    @Test
    public void testReceiveL0SegmentOnlyManifest()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        File expectedResult =
                new File("test/data/segments/l0_segment/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductDto("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsClient, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq("kobs/manifest.safe"),
                Mockito.eq("test/data/segments/l0_segment"));
    }

    @Test
    public void testReceiveL0SegmentObjectStorageException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        this.mockSliceObjectStorageException();
        consumer.receive(
                new ProductDto("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }

    @Test
    public void testReceiveL0SegmentObsUnknownObjectException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        this.mockSliceObsUnknownObjectException();
        consumer.receive(
                new ProductDto("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }
    
    @Test
    public void testReceiveL0SegmentAckException()
            throws ObsException, ObsUnknownObject {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        File expectedResult =
                new File("test/data/segments/l0_segment/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
        consumer.receive(
                new ProductDto("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

}
