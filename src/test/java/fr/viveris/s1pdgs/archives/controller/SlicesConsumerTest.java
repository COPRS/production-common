package fr.viveris.s1pdgs.archives.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import fr.viveris.s1pdgs.archives.controller.dto.SliceDto;
import fr.viveris.s1pdgs.archives.model.ProductFamily;
import fr.viveris.s1pdgs.archives.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.archives.model.exception.ObsUnknownObjectException;
import fr.viveris.s1pdgs.archives.services.ObsService;

public class SlicesConsumerTest {

    @Mock
    private ObsService obsService;
    
    /**
     * Acknowledgement
     */
    @Mock
    private Acknowledgment ack;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
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
                new SlicesConsumer(obsService, "test/data/slices");
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
                new SlicesConsumer(obsService, "test/data/slices");
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
                new SlicesConsumer(obsService, "test/data/slices");
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
                new SlicesConsumer(obsService, "test/data/slices");
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
                new SlicesConsumer(obsService, "test/data/slices");
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
                new SlicesConsumer(obsService, "test/data/slices");
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
                new SlicesConsumer(obsService, "test/data/slices");
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
                new SlicesConsumer(obsService, "test/data/slices");
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
                new SlicesConsumer(obsService, "test/data/slices");
        consumer.receive(
                new SliceDto("productName", "kobs", ProductFamily.BLANK),
                ack, "topic");
    }

}
