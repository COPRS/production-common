package esa.s1pdgs.cpoc.ipf.execution.worker.job.mqi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.ObsQueueMessage;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

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
    private GenericMqiClient sender;

    @Mock
    private ProcessConfiguration processConfiguration;
    
    /**
     * Factory to test
     */
    private OutputProcuderFactory outputProcuderFactory;

    /**
     * Input message
     */
    private GenericMessageDto<IpfExecutionJob> inputMessage;
    
    private final File testDir = FileUtils.createTmpDir();

    /**
     * Iinitialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
        doNothing().when(sender).publish(Mockito.any(), Mockito.any());
        doReturn(AbstractMessage.DEFAULT_HOSTNAME).when(processConfiguration).getHostname();

        this.outputProcuderFactory = new OutputProcuderFactory(sender, processConfiguration);
        inputMessage = new GenericMessageDto<IpfExecutionJob>(
        		123, 
        		"",
                new IpfExecutionJob(ProductFamily.L0_JOB, "product-name", "FAST","work-dir", "job-order")
        );
        inputMessage.setInputKey(null);
    }
    
    @After
    public final void tearDown() throws Exception {
    	FileUtils.delete(testDir.getPath());
    }

    /**
     * Test send L0 reports
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendReport() throws AbstractCodedException {    	
    	final File outputFile = new File(testDir, "report.txt");
    	FileUtils.writeFile(outputFile, "Test report file");
    	
    	final GenericPublicationMessageDto<LevelReportDto> actualMessage = this.outputProcuderFactory.sendOutput(
                new FileQueueMessage(ProductFamily.L0_REPORT, "test.txt", outputFile),
                inputMessage
        );
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_PRODUCTS));
        final LevelReportDto expectedDto = new LevelReportDto("test.txt","Test report file", ProductFamily.L0_REPORT);
        // for equals to work, creation time needs to be equal as well
        expectedDto.setCreationDate(actualMessage.getMessageToPublish().getCreationDate());
        
        final GenericPublicationMessageDto<LevelReportDto> expectedMessage = new GenericPublicationMessageDto<LevelReportDto>(
        		123,
        		ProductFamily.L0_REPORT, 
        		expectedDto
        );        
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_SEGMENTS));
        verify(this.sender, times(1)).publish(Mockito.eq(expectedMessage), Mockito.eq(ProductCategory.LEVEL_REPORTS));
    }

    /**
     * Test send L0 product
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendProduct() throws AbstractCodedException {
    	final GenericPublicationMessageDto<ProductionEvent> actualMessage = this.outputProcuderFactory.sendOutput(
        		new ObsQueueMessage(ProductFamily.L0_SLICE,"test.txt", "test.txt", "NRT"), 
        		inputMessage
        );
    	
    	final ProductionEvent expectedEvent = new ProductionEvent("test.txt","test.txt", ProductFamily.L0_SLICE, "NRT");    	
    	// for equals to work, creation time needs to be equal as well
    	expectedEvent.setCreationDate(actualMessage.getMessageToPublish().getCreationDate());
        final GenericPublicationMessageDto<ProductionEvent> expectedMessage = new GenericPublicationMessageDto<ProductionEvent>(
        		123,
        		ProductFamily.L0_SLICE, 
        		expectedEvent
        );
        expectedMessage.setOutputKey("L0_SLICE");

        verify(this.sender, times(1)).publish(Mockito.eq(expectedMessage), Mockito.eq(ProductCategory.LEVEL_PRODUCTS));
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_REPORTS));
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_SEGMENTS));
    }

    /**
     * Test send L0 product
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendSegment() throws AbstractCodedException {
    	final GenericPublicationMessageDto<ProductionEvent> actualMessage = this.outputProcuderFactory.sendOutput(
        		new ObsQueueMessage(ProductFamily.L0_SEGMENT,"test.txt", "test.txt", "NRT"), 
        		inputMessage
        );
        final ProductionEvent expectedEvent = new ProductionEvent("test.txt","test.txt", ProductFamily.L0_SEGMENT, "NRT");
    	// for equals to work, creation time needs to be equal as well
    	expectedEvent.setCreationDate(actualMessage.getMessageToPublish().getCreationDate());
        final GenericPublicationMessageDto<ProductionEvent> expectedMessage = new GenericPublicationMessageDto<ProductionEvent>(
        		123,
        		ProductFamily.L0_SEGMENT, 
        		expectedEvent
        );        
        verify(this.sender, times(1)).publish(Mockito.eq(expectedMessage), Mockito.eq(ProductCategory.LEVEL_SEGMENTS));
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_REPORTS));
    }

    /**
     * Test send L0 acns
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendAcn() throws AbstractCodedException {
    	final GenericPublicationMessageDto<ProductionEvent> actualMessage = this.outputProcuderFactory.sendOutput(
        		new ObsQueueMessage(ProductFamily.L0_ACN,"test.txt", "test.txt", "FAST"), 
        		inputMessage
        );
        final ProductionEvent expectedEvent = new ProductionEvent("test.txt","test.txt", ProductFamily.L0_ACN, "FAST");
    	// for equals to work, creation time needs to be equal as well
    	expectedEvent.setCreationDate(actualMessage.getMessageToPublish().getCreationDate());
        final GenericPublicationMessageDto<ProductionEvent> expectedMessage = new GenericPublicationMessageDto<ProductionEvent>(
        		123,
        		ProductFamily.L0_ACN, 
        		expectedEvent
        );
        expectedMessage.setOutputKey("L0_ACN");
        
        verify(this.sender, times(1)).publish(Mockito.eq(expectedMessage), Mockito.eq(ProductCategory.LEVEL_PRODUCTS));
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_REPORTS));
    }

    /**
     * Test send L1 report
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendL1Report() throws AbstractCodedException {
    	final File outputFile = new File(testDir, "report.txt");
    	FileUtils.writeFile(outputFile, "Test report file");
        final GenericPublicationMessageDto<LevelReportDto> actualMessage = this.outputProcuderFactory.sendOutput(
        		new FileQueueMessage(ProductFamily.L1_REPORT, "test.txt",outputFile),
        		inputMessage
        );        
        final LevelReportDto expectedDto = new LevelReportDto("test.txt","Test report file", ProductFamily.L1_REPORT);
    	// for equals to work, creation time needs to be equal as well
        expectedDto.setCreationDate(actualMessage.getMessageToPublish().getCreationDate());
        final GenericPublicationMessageDto<LevelReportDto> expectedMessage = new GenericPublicationMessageDto<LevelReportDto>(
        		123,
        		ProductFamily.L1_REPORT, 
        		expectedDto
        );
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_PRODUCTS));
        verify(this.sender, times(1)).publish(Mockito.eq(expectedMessage), Mockito.eq(ProductCategory.LEVEL_REPORTS));       
        assertEquals(expectedMessage, actualMessage);
    }

    /**
     * Test send L1 product
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendL1Product() throws AbstractCodedException {
        final GenericPublicationMessageDto<ProductionEvent> actualMessage = this.outputProcuderFactory.sendOutput(
        		new ObsQueueMessage(ProductFamily.L1_SLICE, "test.txt", "test.txt", "FAST"), 
        		inputMessage
        );
        final ProductionEvent expectedEvent = new ProductionEvent("test.txt", "test.txt", ProductFamily.L1_SLICE, "FAST");        
    	// for equals to work, creation time needs to be equal as well
        expectedEvent.setCreationDate(actualMessage.getMessageToPublish().getCreationDate());
        
        final GenericPublicationMessageDto<ProductionEvent> expectedMessage = new GenericPublicationMessageDto<ProductionEvent>(
        		123,
        		ProductFamily.L1_SLICE, 
        		expectedEvent
        );
        expectedMessage.setOutputKey("L1_SLICE");
        
        verify(this.sender, times(1)).publish(Mockito.eq(expectedMessage), Mockito.eq(ProductCategory.LEVEL_PRODUCTS));
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_REPORTS));        
    }

    /**
     * Test send L1 ACN
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendL1Acn() throws AbstractCodedException {
    	 final GenericPublicationMessageDto<ProductionEvent> actualMessage = this.outputProcuderFactory.sendOutput(
        		new ObsQueueMessage(ProductFamily.L1_ACN,"test.txt", "test.txt", "NRT"), 
        		inputMessage
        );
        final ProductionEvent expectedEvent = new ProductionEvent("test.txt","test.txt", ProductFamily.L1_ACN, "NRT");
    	// for equals to work, creation time needs to be equal as well
        expectedEvent.setCreationDate(actualMessage.getMessageToPublish().getCreationDate());
        final GenericPublicationMessageDto<ProductionEvent> expectedMessage = new GenericPublicationMessageDto<ProductionEvent>(
        		123,
        		ProductFamily.L1_ACN, 
        		expectedEvent
        );
        expectedMessage.setOutputKey("L1_ACN");
        
        verify(this.sender, times(1)).publish(Mockito.eq(expectedMessage), Mockito.eq(ProductCategory.LEVEL_PRODUCTS));
        verify(this.sender, never()).publish(Mockito.any(), Mockito.eq(ProductCategory.LEVEL_REPORTS));  
    }
}
