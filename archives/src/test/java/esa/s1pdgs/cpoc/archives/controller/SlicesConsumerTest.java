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

public class SlicesConsumerTest {

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
        doReturn(result).when(obsClient).download(Mockito.anyList());
    }

    private void mockSliceObjectStorageException()
            throws AbstractCodedException {
        doThrow(new ObsException(ProductFamily.L0_SLICE, "kobs",
                new Throwable())).when(obsClient).download(Mockito.anyList());
    }

    private void mockSliceObsUnknownObjectException()
            throws AbstractCodedException {
        doThrow(new ObsUnknownObject(ProductFamily.BLANK, "kobs"))
                .when(obsClient).download(Mockito.anyList());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testReceiveL0Slice()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/slices/l0_slice/productName"));
        mockDevProperties(true);
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(new ObsDownloadObject(
        		ProductFamily.L0_SLICE, "kobs", "test/data/slices/l0_slice"))));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testReceiveL0SliceOnlyManifest()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/slices/l0_slice/productName"));
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(new ObsDownloadObject(
        		ProductFamily.L0_SLICE, "kobs/manifest.safe", "test/data/slices/l0_slice"))));
    }

    @Test
    public void testReceiveL0SliceObjectStorageException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        this.mockSliceObjectStorageException();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }

    @Test
    public void testReceiveL0SliceObsUnknownObjectException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        this.mockSliceObsUnknownObjectException();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }
    
    @Test
    public void testReceiveL0SliceAckException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/slices/l0_slice/productName"));
        this.mockSliceDownloadFiles(expectedResult);
        doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L0_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

    @Test
    public void testReceiveL1Slice()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/slices/l1_slice/productName"));
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L1_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(new ObsDownloadObject(
        		ProductFamily.L1_SLICE, "kobs/manifest.safe", "test/data/slices/l1_slice"))));
    }

    @Test
    public void testReceiveL1SliceObjectStorageException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        this.mockSliceObjectStorageException();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L1_SLICE, "NRT"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }

    @Test
    public void testReceiveL1SliceObsUnknownObjectException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        this.mockSliceObsUnknownObjectException();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L1_SLICE, "NRT"),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }
    
    @Test
    public void testReceiveL1SliceAckException()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/slices/l1_slice/productName"));
        this.mockSliceDownloadFiles(expectedResult);
        doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L1_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

    @Test
    public void testReceiveUnknownSlice() throws ObsException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.BLANK, "NRT"),
                ack, "topic");
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void testReceiveL2Slice()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/slices/l2_slice/productName"));
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L2_SLICE, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(new ObsDownloadObject(
        		ProductFamily.L2_SLICE, "kobs/manifest.safe", "test/data/slices/l2_slice"))));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void testReceiveL2Acn()
            throws AbstractCodedException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsClient, "test/data/slices", devProperties,
                        appStatus);
        List<File> expectedResult = Arrays.asList(new File("test/data/slices/l2_acn/productName"));
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new ProductionEvent("productName", "kobs", ProductFamily.L2_ACN, "NRT"),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
        verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(new ObsDownloadObject(
        		ProductFamily.L2_ACN, "kobs/manifest.safe", "test/data/slices/l2_acn"))));
    }

}
