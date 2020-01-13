package esa.s1pdgs.cpoc.archives.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.archives.DevProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.Reporting;

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

    private void mockSliceDownloadFiles(List<File> result)
            throws AbstractCodedException {
        doReturn(result).when(obsClient).download(Mockito.anyList(), Reporting.ChildFactory.NULL);
    }

    private void mockSliceObjectStorageException()
            throws AbstractCodedException {
        doThrow(new ObsException(ProductFamily.L0_SEGMENT, "kobs",
                new Throwable())).when(obsClient).download(Mockito.anyList(), Reporting.ChildFactory.NULL);
    }

    private void mockSliceObsUnknownObjectException()
            throws AbstractCodedException {
        doThrow(new ObsUnknownObject(ProductFamily.BLANK, "kobs"))
                .when(obsClient).download(Mockito.anyList(), Reporting.ChildFactory.NULL);
    }

	@Test
	@SuppressWarnings("unchecked")
    public void testReceiveL0Segment()
            throws AbstractCodedException {
        SegmentsConsumer consumer =
                new SegmentsConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/segments/l0_segment/productName"));
        mockDevProperties(true);
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        
        verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(new ObsDownloadObject(
        		ProductFamily.L0_SEGMENT, "kobs","test/data/segments/l0_segment"))), Reporting.ChildFactory.NULL);
    }

    @Test
	@SuppressWarnings("unchecked")
    public void testReceiveL0SegmentOnlyManifest()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/segments/l0_segment/productName"));
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(new ObsDownloadObject(
        		ProductFamily.L0_SEGMENT, "kobs/manifest.safe", "test/data/segments/l0_segment"))), Reporting.ChildFactory.NULL);
    }

    @Test
    public void testReceiveL0SegmentObjectStorageException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        this.mockSliceObjectStorageException();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }

    @Test
    public void testReceiveL0SegmentObsUnknownObjectException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        this.mockSliceObsUnknownObjectException();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }
    
    @Test
    public void testReceiveL0SegmentAckException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/segments", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/segments/l0_segment/productName"));
        this.mockSliceDownloadFiles(expectedResult);
        doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SEGMENT, "FAST"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

}
