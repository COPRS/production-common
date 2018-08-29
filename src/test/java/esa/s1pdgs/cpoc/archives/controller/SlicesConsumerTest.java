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
import esa.s1pdgs.cpoc.archives.controller.SlicesConsumer;
import esa.s1pdgs.cpoc.archives.controller.dto.SliceDto;
import esa.s1pdgs.cpoc.archives.model.ProductFamily;
import esa.s1pdgs.cpoc.archives.model.exception.ObjectStorageException;
import esa.s1pdgs.cpoc.archives.model.exception.ObsUnknownObjectException;
import esa.s1pdgs.cpoc.archives.services.ObsService;

public class SlicesConsumerTest {

    @Mock
    private ObsService obsService;
    
    @Mock
    private DevProperties devProperties;
    
    /**
     * Acknowledgement
     */
    @Mock
    private Acknowledgment ack;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockDevProperties(true);
    }
    
    private void mockDevProperties(boolean act) {
        Map<String, Boolean> activations = new HashMap<>();
        activations.put("download-manifest", act);
        doReturn(activations).when(devProperties).getActivations();
    }

    private void mockSliceDownloadFiles(File result)
            throws ObjectStorageException, ObsUnknownObjectException {
        doReturn(result).when(obsService).downloadFile(
                Mockito.any(ProductFamily.class), Mockito.anyString(),
                Mockito.anyString());
    }

    private void mockSliceObjectStorageException()
            throws ObjectStorageException, ObsUnknownObjectException {
        doThrow(new ObjectStorageException(ProductFamily.L0_PRODUCT, "kobs",
                new Throwable())).when(obsService).downloadFile(
                        Mockito.any(ProductFamily.class), Mockito.anyString(),
                        Mockito.anyString());
    }

    private void mockSliceObsUnknownObjectException()
            throws ObsUnknownObjectException, ObjectStorageException {
        doThrow(new ObsUnknownObjectException(ProductFamily.UNKNOWN, "kobs"))
                .when(obsService).downloadFile(Mockito.any(ProductFamily.class),
                        Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testReceiveL0Slice()
            throws ObjectStorageException, ObsUnknownObjectException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        File expectedResult =
                new File("test/data/slices/l0_product/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.L0_PRODUCT),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

    @Test
    public void testReceiveL0SliceObjectStorageException()
            throws ObjectStorageException, ObsUnknownObjectException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        this.mockSliceObjectStorageException();
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.L0_PRODUCT),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }

    @Test
    public void testReceiveL0SliceObsUnknownObjectException()
            throws ObjectStorageException, ObsUnknownObjectException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        this.mockSliceObsUnknownObjectException();
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.L0_PRODUCT),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }
    
    @Test
    public void testReceiveL0SliceAckException()
            throws ObjectStorageException, ObsUnknownObjectException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        File expectedResult =
                new File("test/data/slices/l0_product/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.L0_PRODUCT),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

    @Test
    public void testReceiveL1Slice()
            throws ObjectStorageException, ObsUnknownObjectException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        File expectedResult =
                new File("test/data/slices/l1_product/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doNothing().when(ack).acknowledge();
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.L1_PRODUCT),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

    @Test
    public void testReceiveL1SliceObjectStorageException()
            throws ObjectStorageException, ObsUnknownObjectException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        this.mockSliceObjectStorageException();
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.L1_PRODUCT),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }

    @Test
    public void testReceiveL1SliceObsUnknownObjectException()
            throws ObjectStorageException, ObsUnknownObjectException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        this.mockSliceObsUnknownObjectException();
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.L1_PRODUCT),
                ack, "topic");
        verify(ack, never()).acknowledge();
    }
    
    @Test
    public void testReceiveL1SliceAckException()
            throws ObjectStorageException, ObsUnknownObjectException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        File expectedResult =
                new File("test/data/slices/l1_product/productName");
        this.mockSliceDownloadFiles(expectedResult);
        doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.L1_PRODUCT),
                ack, "topic");
        verify(ack, times(1)).acknowledge();
    }

    @Test
    public void testReceiveUnknownSlice() throws ObjectStorageException {
        SlicesConsumer consumer =
                new SlicesConsumer(obsService, "test/data/slices", devProperties);
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.BLANK),
                ack, "topic");
    }

}
