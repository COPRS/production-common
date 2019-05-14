package esa.s1pdgs.cpoc.wrapper.job.mqi;

import static org.mockito.Mockito.doNothing;
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

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.client.ErrorService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.ObsQueueMessage;

/**
 * Test the factory for producer message in topics
 * 
 * @author Viveris Technologies
 */
public class OutputProducerFactoryTest {

    /**
     * Kafka producer for segments
     */
    @Mock
    private GenericMqiService<LevelSegmentDto> senderSegments;

    /**
     * Kafka producer for L0 slices
     */
    @Mock
    private GenericMqiService<LevelProductDto> senderProducts;

    /**
     * Kafka producer for report
     */
    @Mock
    private GenericMqiService<LevelReportDto> senderReports;

    /**
     * Kafka producer for errors
     */
    @Mock
    private ErrorService errorService;

    /**
     * Factory to test
     */
    private OutputProcuderFactory outputProcuderFactory;

    /**
     * Input message
     */
    private GenericMessageDto<LevelJobDto> inputMessage;

    /**
     * Iinitialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
        doNothing().when(senderProducts).publish(Mockito.any());
        doNothing().when(senderReports).publish(Mockito.any());
        doNothing().when(errorService).publish(Mockito.any());
        this.outputProcuderFactory = new OutputProcuderFactory(senderSegments,
                senderProducts, senderReports, errorService);
        inputMessage = new GenericMessageDto<LevelJobDto>(123, "",
                new LevelJobDto(ProductFamily.L0_JOB, "product-name", "FAST",
                        "work-dir", "job-order"));
        inputMessage.setInputKey(null);
    }

    /**
     * Test send L0 reports
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendReport() throws AbstractCodedException {
        this.outputProcuderFactory.sendOutput(
                new FileQueueMessage(ProductFamily.L0_REPORT, "test.txt",
                        new File("./test/data/report.txt")),
                inputMessage);
        verify(this.senderProducts, never()).publish(Mockito.any());
        GenericPublicationMessageDto<LevelReportDto> message =
                new GenericPublicationMessageDto<LevelReportDto>(123,
                        ProductFamily.L0_REPORT, new LevelReportDto("test.txt",
                                "Test report file", ProductFamily.L0_REPORT));
        verify(this.senderReports, times(1)).publish(Mockito.eq(message));
        verify(this.senderSegments, never()).publish(Mockito.any());
    }

    /**
     * Test send L0 product
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendProduct() throws AbstractCodedException {
        this.outputProcuderFactory
                .sendOutput(new ObsQueueMessage(ProductFamily.L0_SLICE,
                        "test.txt", "test.txt", "NRT"), inputMessage);
        GenericPublicationMessageDto<LevelProductDto> message =
                new GenericPublicationMessageDto<LevelProductDto>(123,
                        ProductFamily.L0_SLICE, new LevelProductDto("test.txt",
                                "test.txt", ProductFamily.L0_SLICE, "NRT"));
        message.setOutputKey("L0_SLICE");
        verify(this.senderProducts, times(1)).publish(Mockito.eq(message));
        verify(this.senderReports, never()).publish(Mockito.any());
        verify(this.senderSegments, never()).publish(Mockito.any());
    }

    /**
     * Test send L0 product
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendSegment() throws AbstractCodedException {
        this.outputProcuderFactory
                .sendOutput(new ObsQueueMessage(ProductFamily.L0_SEGMENT,
                        "test.txt", "test.txt", "NRT"), inputMessage);
        GenericPublicationMessageDto<LevelSegmentDto> message =
                new GenericPublicationMessageDto<LevelSegmentDto>(123,
                        ProductFamily.L0_SEGMENT, new LevelSegmentDto("test.txt",
                                "test.txt", ProductFamily.L0_SEGMENT, "NRT"));
        verify(this.senderSegments, times(1)).publish(Mockito.eq(message));
        verify(this.senderProducts, never()).publish(Mockito.any());
        verify(this.senderReports, never()).publish(Mockito.any());
    }

    /**
     * Test send L0 acns
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendAcn() throws AbstractCodedException {
        this.outputProcuderFactory
                .sendOutput(new ObsQueueMessage(ProductFamily.L0_ACN,
                        "test.txt", "test.txt", "FAST"), inputMessage);
        GenericPublicationMessageDto<LevelProductDto> message =
                new GenericPublicationMessageDto<LevelProductDto>(123,
                        ProductFamily.L0_ACN, new LevelProductDto("test.txt",
                                "test.txt", ProductFamily.L0_ACN, "FAST"));
        message.setOutputKey("L0_ACN");
        verify(this.senderProducts, times(1)).publish(Mockito.eq(message));
        verify(this.senderReports, never()).publish(Mockito.any());
    }

    /**
     * Test send L1 report
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendL1Report() throws AbstractCodedException {
        this.outputProcuderFactory.sendOutput(
                new FileQueueMessage(ProductFamily.L1_REPORT, "test.txt",
                        new File("./test/data/report.txt")),
                inputMessage);
        GenericPublicationMessageDto<LevelReportDto> message =
                new GenericPublicationMessageDto<LevelReportDto>(123,
                        ProductFamily.L1_REPORT, new LevelReportDto("test.txt",
                                "Test report file", ProductFamily.L1_REPORT));
        verify(this.senderProducts, never()).publish(Mockito.any());
        verify(this.senderReports, times(1)).publish(Mockito.eq(message));
    }

    /**
     * Test send L1 product
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendL1Product() throws AbstractCodedException {
        this.outputProcuderFactory
                .sendOutput(new ObsQueueMessage(ProductFamily.L1_SLICE,
                        "test.txt", "test.txt", "FAST"), inputMessage);
        GenericPublicationMessageDto<LevelProductDto> message =
                new GenericPublicationMessageDto<LevelProductDto>(123,
                        ProductFamily.L1_SLICE, new LevelProductDto("test.txt",
                                "test.txt", ProductFamily.L1_SLICE, "FAST"));
        message.setOutputKey("L1_SLICE");
        verify(this.senderProducts, times(1)).publish(Mockito.eq(message));
        verify(this.senderReports, never()).publish(Mockito.any());
    }

    /**
     * Test send L1 ACN
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendL1Acn() throws AbstractCodedException {
        this.outputProcuderFactory
                .sendOutput(new ObsQueueMessage(ProductFamily.L1_ACN,
                        "test.txt", "test.txt", "NRT"), inputMessage);
        GenericPublicationMessageDto<LevelProductDto> message =
                new GenericPublicationMessageDto<LevelProductDto>(123,
                        ProductFamily.L1_ACN, new LevelProductDto("test.txt",
                                "test.txt", ProductFamily.L1_ACN, "NRT"));
        message.setOutputKey("L1_ACN");
        verify(this.senderProducts, times(1)).publish(Mockito.eq(message));
        verify(this.senderReports, never()).publish(Mockito.any());
    }

    /**
     * Test send error
     */
    @Test
    public void testSendError() throws AbstractCodedException {
        this.outputProcuderFactory.sendError("error message");
        verify(this.errorService, times(1))
                .publish(Mockito.eq("error message"));
        verify(this.senderReports, never()).publish(Mockito.any());
        verify(this.senderProducts, never()).publish(Mockito.any());
    }

    /**
     * Test send error
     */
    @Test
    public void testSendErrorWhenException() throws AbstractCodedException {
        doThrow(new InternalErrorException("execption raised"))
                .when(errorService).publish(Mockito.anyString());
        this.outputProcuderFactory.sendError("error message");
        verify(this.errorService, times(1))
                .publish(Mockito.eq("error message"));
        verify(this.senderReports, never()).publish(Mockito.any());
        verify(this.senderProducts, never()).publish(Mockito.any());
    }
}
