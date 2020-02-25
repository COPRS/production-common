package esa.s1pdgs.cpoc.ipf.execution.worker.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfExecutionWorkerProcessTimeoutException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.execution.worker.TestUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.InputDownloader;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.OutputProcessor;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.ipf.execution.worker.test.MockPropertiesTest;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;

/**
 * Test the job processor
 * 
 * @author Viveris Technologies
 */
public class JobProcessorTest extends MockPropertiesTest {

    /**
     * Output processsor
     */
    @Mock
    private OutputProcuderFactory procuderFactory;

    /**
     * Output processsor
     */
    @Mock
    private ObsClient obsClient;

    /**
     * MQI service
     */
    @Mock
    private GenericMqiClient mqiService;

    /**
     * Job to process
     */
    private GenericMessageDto<IpfExecutionJob> inputMessage;

    /**
     * Processor to test
     */
    private JobProcessor processor;

    /**
     * Working directory
     */
    private File workingDir;

    @Mock
    private InputDownloader inputDownloader;

    @Mock
    private OutputProcessor outputProcessor;

    @Mock
    private ExecutorService procExecutorSrv;

    @Mock
    private ExecutorCompletionService<Void> procCompletionSrv;

    @Mock
    private PoolExecutorCallable procExecutor;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("TestOutputHandling");
	
    private final ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL;

    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        mockDefaultAppProperties();
        mockDefaultDevProperties();
        mockDefaultStatus();
        //devProperties.getStepsActivation().put("erasing", Boolean.FALSE);

        inputMessage = new GenericMessageDto<IpfExecutionJob>(123, "",
                TestUtils.buildL0IpfExecutionJob());
        workingDir = new File(inputMessage.getBody().getWorkDirectory());
        if (!workingDir.exists()) {
            workingDir.mkdir();
        }
        mockWorkingdirProperties(workingDir.toPath());
        processor = new JobProcessor(appStatus, properties, devProperties,
                obsClient, procuderFactory, mqiService, errorAppender, mqiStatusService, 0L, 10L);
        procExecutorSrv = Executors.newSingleThreadExecutor();
        procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);
    }

    /**
     * Clean
     */
    @After
    public void clean() {
        if (workingDir.exists()) {
            workingDir.delete();
        }
    }

    /**
     * Test processPoolProcesses when call raises a custom exception
     * 
     * @throws AbstractCodedException
     * @throws InterruptedException
     */
    @Test
    public void waitprocessPoolProcessesWhenCustomException()
            throws AbstractCodedException, InterruptedException {
        doThrow(new IpfExecutionWorkerProcessTimeoutException("timeout exception"))
                .when(procExecutor).call();
        final ExecutorCompletionService<Void> procCompletionSrvTmp =
                new ExecutorCompletionService<>(
                        Executors.newSingleThreadExecutor());
        procCompletionSrvTmp.submit(procExecutor);

        thrown.expect(IpfExecutionWorkerProcessTimeoutException.class);
        thrown.expectMessage("timeout exception");
        processor.waitForPoolProcessesEnding(procCompletionSrvTmp);
    }

    /**
     * Test processPoolProcesses when call raises a not-custom exception
     * 
     * @throws AbstractCodedException
     * @throws InterruptedException
     */
    @Test
    public void waitprocessPoolProcessesWhenOtherException()
            throws AbstractCodedException, InterruptedException {
        doThrow(new IllegalArgumentException("other exception"))
                .when(procExecutor).call();
        final ExecutorCompletionService<Void> procCompletionSrvTmp =
                new ExecutorCompletionService<>(
                        Executors.newSingleThreadExecutor());
        procCompletionSrvTmp.submit(procExecutor);

        thrown.expect(InternalErrorException.class);
        processor.waitForPoolProcessesEnding(procCompletionSrvTmp);
    }

    @Test
    public void testCleanJobProcessing() throws IOException {
        final File folder1 =
                new File(inputMessage.getBody().getWorkDirectory() + "folder1");
        folder1.mkdir();
        final File file1 = new File(inputMessage.getBody().getWorkDirectory()
                + "folder1" + File.separator + "file1");
        file1.createNewFile();
        final File file2 =
                new File(inputMessage.getBody().getWorkDirectory() + "file2");
        file2.createNewFile();
        assertTrue(workingDir.exists());
        assertTrue(file1.exists());      
        processor.cleanJobProcessing(inputMessage.getBody(), true, procExecutorSrv);

        verify(properties, times(1)).getTmProcStopS();
        assertFalse(workingDir.exists());
    }

    /**
     * Mock all steps
     * 
     * @param simulateError
     *            if true an error is raised by the method call of the processes
     *            executor
     * @throws Exception
     */
    private void mockAllStep(final boolean simulateError) throws Exception {
        // Step 3
        if (simulateError) {
            doThrow(new IpfExecutionWorkerProcessTimeoutException("timeout exception"))
                    .when(procExecutor).call();
        } 
        // Step 2
        doNothing().when(inputDownloader).processInputs(reporting);
        // Step 4
        doReturn(ReportingOutput.NULL).when(outputProcessor).processOutput(reporting, UUID.randomUUID());
        // Step 5
        final File folder1 =
                new File(inputMessage.getBody().getWorkDirectory() + "folder1");
        folder1.mkdir();
        final File file1 = new File(inputMessage.getBody().getWorkDirectory()
                + "folder1" + File.separator + "file1");
        file1.createNewFile();
        final File file2 =
                new File(inputMessage.getBody().getWorkDirectory() + "file2");
        file2.createNewFile();
    }

    /**
     * Nominal test case of call
     * 
     * @throws Exception
     */
    public void testCallWithNext() throws Exception {
        mockAllStep(false);
        doReturn(ApplicationLevel.L0).when(properties).getLevel();
        processor.onMessage(inputMessage);
        doReturn(ApplicationLevel.L1).when(properties).getLevel();
    }
    
    
    /**
     * Nominal test case of call
     * 
     * @throws Exception
     */
    @Test
    public void testCall() throws Exception {
        mockAllStep(false);
        
        

        processor.processJob(inputMessage, inputDownloader, outputProcessor,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs(reporting);
        // Check step 4
        verify(outputProcessor, times(1)).processOutput(reporting, reporting.getUid());
       
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(0)).getTmProcStopS();
    }

    /**
     * Nominal test case of call when download is deactivated
     * 
     * @throws Exception
     */
    @Test
    public void testCallStepDownloadNotActive() throws Exception {
        mockAllStep(false);
        mockDevProperties(false, true, true, true);

        processor.processJob(inputMessage, inputDownloader, outputProcessor,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(0)).processInputs(reporting);
        // Check step 4
        verify(outputProcessor, times(1)).processOutput(reporting, reporting.getUid());
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(0)).getTmProcStopS();
    }

    /**
     * Nominal test case of call when output processing is deactivated
     * 
     * @throws Exception
     */
    @Test
    public void testCallStepOutputNotActive() throws Exception {
        mockAllStep(false);
        mockDevProperties(true, true, false, true);

        processor.processJob(inputMessage, inputDownloader, outputProcessor,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs(reporting);
        // Check step 4
        verify(outputProcessor, times(0)).processOutput(reporting, UUID.randomUUID());
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(0)).getTmProcStopS();
    }

    /**
     * Nominal test case of call when erasing is deactivated
     * 
     * @throws Exception
     */

    @Test
    public void testCallStepErasingNotActive() throws Exception {
        mockAllStep(false);
        mockDevProperties(true, true, true, false);

        processor.processJob(inputMessage, inputDownloader, outputProcessor,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs(reporting);
        // Check step 4
        verify(outputProcessor, times(1)).processOutput(reporting, reporting.getUid());
        // Check step 5
        assertTrue(workingDir.exists());
        // Check step 6
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(0)).getTmProcStopS();

        // REexcute erase to purge test folder
        processor.cleanJobProcessing(inputMessage.getBody(), false,
                procExecutorSrv);
    }

    /**
     * Test call when an exception during processes execution
     * 
     * @throws Exception
     */
    public void testCallWhenException() throws Exception {
        mockAllStep(true);

        processor.processJob(inputMessage, inputDownloader, outputProcessor,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs(reporting);
        // Check status set to error
        verify(appStatus, times(1)).setError("PROCESSING");
        // Check step 4
        verify(outputProcessor, never()).processOutput(reporting, UUID.randomUUID());
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(1)).getTmProcStopS();

    }
}
