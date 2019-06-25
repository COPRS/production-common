package esa.s1pdgs.cpoc.compression.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.processing.WrapperProcessTimeoutException;
import esa.s1pdgs.cpoc.compression.TestUtils;
import esa.s1pdgs.cpoc.compression.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.file.FileUploader;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.compression.test.MockPropertiesTest;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

//public class CompressProcessorTest extends MockPropertiesTest {
//	/**
//     * Output processsor
//     */
//    @Mock
//    private ObsService obsService;
//    
//    @Mock
//    private FileDownloader fileDownloader;
//    
//    @Mock
//    private FileUploader fileUploader;
//
//    /**
//     * MQI service
//     */
//    @Mock
//    private GenericMqiService<LevelJobDto> mqiService;
//
//    /**
//     * Job to process
//     */
//    private GenericMessageDto<ProductDto> inputMessage;
//
//    /**
//     * Processor to test
//     */
//    private CompressProcessor processor;
//    
//    /**
//     * Working directory
//     */
//    private File workingDir;
//    
//    @Mock
//    private ExecutorService procExecutorSrv;
//
//    @Mock
//    private ExecutorCompletionService<Boolean> procCompletionSrv;
//
//    @Mock
//    private PoolExecutorCallable procExecutor;
//    
//    /**
//     * To check the raised custom exceptions
//     */
//    @Rule
//    public ExpectedException thrown = ExpectedException.none();
//    
//    /**
//     * Initialization
//     * 
//     * @throws AbstractCodedException
//     */
//    @Before
//    public void init() throws AbstractCodedException {
//        MockitoAnnotations.initMocks(this);
//
//        mockDefaultAppProperties();
//        mockDefaultDevProperties();
//        mockDefaultStatus();
//
//        inputMessage = new GenericMessageDto<LevelJobDto>(123, "",
//                TestUtils.buildL0LevelJobDto());
//        workingDir = new File(inputMessage.getBody().getWorkDirectory());
//        if (!workingDir.exists()) {
//            workingDir.mkdir();
//        }
//        //TODO FIX
////        processor = new CompressProcessor(appStatus, properties,
////                obsService,mqiService, mqiStatusService);
//        
//        procExecutorSrv = Executors.newSingleThreadExecutor();
//        procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);
//    }
//
//    /**
//     * Clean
//     */
//    @After
//    public void clean() {
//        if (workingDir.exists()) {
//            workingDir.delete();
//        }
//    }
//    
//    /**
//     * Test when application shall be stopped
//     */
//    @Test
//    public void testProcessJobWhenAppShallBeStopped() {
//        doReturn(true).when(appStatus).isShallBeStopped();
//
//        processor.processTask();
//
//        verify(appStatus, times(1)).forceStopping();
//        verifyZeroInteractions(mqiService);
//    }
//    
//    /**
//     * Test ack when exception
//     * 
//     * @throws AbstractCodedException
//     */
//    @Test
//    public void testAckNegativelyWhenException() throws AbstractCodedException {
//        doThrow(new MqiAckApiError(ProductCategory.AUXILIARY_FILES, 1,
//                "ack-msg", "error-ùmessage")).when(mqiService)
//                        .ack(Mockito.any());
//
//        processor.ackNegatively(false, inputMessage, "error message");
//
//        verify(mqiService, times(1)).ack(Mockito
//                .eq(new AckMessageDto(123, Ack.ERROR, "error message", false)));
//        verify(appStatus, times(1)).setError("PROCESSING");
//    }
//    
//    /**
//     * Test ack when exception
//     * 
//     * @throws AbstractCodedException
//     */
//    @Test
//    public void testAckPositivelyWhenException() throws AbstractCodedException {
//        doThrow(new MqiAckApiError(ProductCategory.AUXILIARY_FILES, 1,
//                "ack-msg", "error-message")).when(mqiService)
//                        .ack(Mockito.any());
//
//        processor.ackPositively(false, inputMessage);
//
//        verify(mqiService, times(1))
//                .ack(Mockito.eq(new AckMessageDto(123, Ack.OK, null, false)));
//        verify(appStatus, times(1)).setError("PROCESSING");
//    }
//    
//    /**
//     * Test processPoolProcesses when call raises a custom exception
//     * 
//     * @throws AbstractCodedException
//     * @throws InterruptedException
//     */
//    @Test
//    public void waitprocessPoolProcessesWhenCustomException()
//            throws AbstractCodedException, InterruptedException {
//        doThrow(new WrapperProcessTimeoutException("timeout exception"))
//                .when(procExecutor).call();
//        ExecutorCompletionService<Boolean> procCompletionSrvTmp =
//                new ExecutorCompletionService<>(
//                        Executors.newSingleThreadExecutor());
//        procCompletionSrvTmp.submit(procExecutor);
//        Thread.sleep(500);
//
//        thrown.expect(WrapperProcessTimeoutException.class);
//        thrown.expectMessage("timeout exception");
//        processor.waitForPoolProcessesEnding(procCompletionSrvTmp);
//    }
//    
//    /**
//     * Test processPoolProcesses when call raises a not-custom exception
//     * 
//     * @throws AbstractCodedException
//     * @throws InterruptedException
//     */
//    @Test
//    public void waitprocessPoolProcessesWhenOtherException()
//            throws AbstractCodedException, InterruptedException {
//        doThrow(new IllegalArgumentException("other exception"))
//                .when(procExecutor).call();
//        ExecutorCompletionService<Boolean> procCompletionSrvTmp =
//                new ExecutorCompletionService<>(
//                        Executors.newSingleThreadExecutor());
//        procCompletionSrvTmp.submit(procExecutor);
//        Thread.sleep(500);
//
//        thrown.expect(InternalErrorException.class);
//        processor.waitForPoolProcessesEnding(procCompletionSrvTmp);
//    }
//    
//    @Test
//    public void testCleanJobProcessing() throws IOException {
//        File folder1 =
//                new File(inputMessage.getBody().getWorkDirectory() + "folder1");
//        folder1.mkdir();
//        File file1 = new File(inputMessage.getBody().getWorkDirectory()
//                + "folder1" + File.separator + "file1");
//        file1.createNewFile();
//        File file2 =
//                new File(inputMessage.getBody().getWorkDirectory() + "file2");
//        file2.createNewFile();
//        assertTrue(workingDir.exists());
//        assertTrue(file1.exists());
//
//        processor.cleanJobProcessing(inputMessage.getBody(), true,
//                procExecutorSrv);
//
//        verify(properties, times(1)).getTmProcStopS();
//        assertFalse(workingDir.exists());
//    }
//    
//    /**
//     * Mock all steps
//     * 
//     * @param simulateError
//     *            if true an error is raised by the method call of the processes
//     *            executor
//     * @throws Exception
//     */
//    private void mockAllStep(boolean simulateError) throws Exception {
//        // Step 3
//        if (simulateError) {
//            doThrow(new WrapperProcessTimeoutException("timeout exception"))
//                    .when(procExecutor).call();
//        } else {
//            doReturn(true).when(procExecutor).call();
//        }
//        // Step 2
//        doNothing().when(fileDownloader).processInputs();
//        // Step 4
//        doNothing().when(fileUploader).processOutput();
//        // Step 5
//        File folder1 =
//                new File(inputMessage.getBody().getWorkDirectory() + "folder1");
//        folder1.mkdir();
//        File file1 = new File(inputMessage.getBody().getWorkDirectory()
//                + "folder1" + File.separator + "file1");
//        file1.createNewFile();
//        File file2 =
//                new File(inputMessage.getBody().getWorkDirectory() + "file2");
//        file2.createNewFile();
//    }
//    
//    /**
//     * Nominal test case of call
//     * 
//     * @throws Exception
//     */
//    @Test
//    public void testCall() throws Exception {
//        mockAllStep(false);
//        
//        
//
////        processor.processJob(inputMessage, fileDownloader, fileUploader,
////                procExecutorSrv, procCompletionSrv, procExecutor, reportingFactory.newReporting(0));
//        
//        processor.processTask();
//
//        // Check step 3
//        verify(procExecutor, times(1)).call();
//        // Check step 2
//        verify(fileDownloader, times(1)).processInputs();
//        // Check step 4
//        verify(fileUploader, times(1)).processOutput();
//        // Check step 5
//        assertFalse(workingDir.exists());
//        // Check step 6
//        verify(appStatus, times(1)).setWaiting();
//        // Check properties call
//        verify(properties, times(1)).getTmProcAllTasksS();
//        verify(properties, times(0)).getTmProcStopS();
//    }
//}
