package esa.s1pdgs.cpoc.wrapper.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.WrapperProcessTimeoutException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.wrapper.TestUtils;
import esa.s1pdgs.cpoc.wrapper.job.JobProcessor;
import esa.s1pdgs.cpoc.wrapper.job.file.InputDownloader;
import esa.s1pdgs.cpoc.wrapper.job.file.OutputProcessor;
import esa.s1pdgs.cpoc.wrapper.job.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.wrapper.job.obs.ObsService;
import esa.s1pdgs.cpoc.wrapper.job.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.wrapper.test.MockPropertiesTest;

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
    private ObsService obsService;

    /**
     * MQI service
     */
    @Mock
    private GenericMqiService<LevelJobDto> mqiService;

    /**
     * Job to process
     */
    private GenericMessageDto<LevelJobDto> inputMessage;

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
    private ExecutorCompletionService<Boolean> procCompletionSrv;

    @Mock
    private PoolExecutorCallable procExecutor;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        mockDefaultAppProperties();
        mockDefaultDevProperties();
        mockDefaultStatus();

        inputMessage = new GenericMessageDto<LevelJobDto>(123, "",
                TestUtils.buildL0LevelJobDto());
        workingDir = new File(inputMessage.getBody().getWorkDirectory());
        if (!workingDir.exists()) {
            workingDir.mkdir();
        }
        processor = new JobProcessor(appStatus, properties, devProperties,
                obsService, procuderFactory, mqiService);
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
     * Test when application shall be stopped
     */
    @Test
    public void testProcessJobWhenAppShallBeStopped() {
        doReturn(true).when(appStatus).isShallBeStopped();

        processor.processJob();

        verify(appStatus, times(1)).forceStopping();
        verifyZeroInteractions(mqiService);
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
        doThrow(new WrapperProcessTimeoutException("timeout exception"))
                .when(procExecutor).call();
        ExecutorCompletionService<Boolean> procCompletionSrvTmp =
                new ExecutorCompletionService<>(
                        Executors.newSingleThreadExecutor());
        procCompletionSrvTmp.submit(procExecutor);
        Thread.sleep(500);

        thrown.expect(WrapperProcessTimeoutException.class);
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
        ExecutorCompletionService<Boolean> procCompletionSrvTmp =
                new ExecutorCompletionService<>(
                        Executors.newSingleThreadExecutor());
        procCompletionSrvTmp.submit(procExecutor);
        Thread.sleep(500);

        thrown.expect(InternalErrorException.class);
        processor.waitForPoolProcessesEnding(procCompletionSrvTmp);
    }

    @Test
    public void testCleanJobProcessing() throws IOException {
        File folder1 =
                new File(inputMessage.getBody().getWorkDirectory() + "folder1");
        folder1.mkdir();
        File file1 = new File(inputMessage.getBody().getWorkDirectory()
                + "folder1" + File.separator + "file1");
        file1.createNewFile();
        File file2 =
                new File(inputMessage.getBody().getWorkDirectory() + "file2");
        file2.createNewFile();
        assertTrue(workingDir.exists());
        assertTrue(file1.exists());

        processor.cleanJobProcessing(inputMessage.getBody(), true,
                procExecutorSrv);

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
    private void mockAllStep(boolean simulateError) throws Exception {
        // Step 3
        if (simulateError) {
            doThrow(new WrapperProcessTimeoutException("timeout exception"))
                    .when(procExecutor).call();
        } else {
            doReturn(true).when(procExecutor).call();
        }
        // Step 2
        doNothing().when(inputDownloader).processInputs();
        // Step 4
        doNothing().when(outputProcessor).processOutput();
        // Step 5
        File folder1 =
                new File(inputMessage.getBody().getWorkDirectory() + "folder1");
        folder1.mkdir();
        File file1 = new File(inputMessage.getBody().getWorkDirectory()
                + "folder1" + File.separator + "file1");
        file1.createNewFile();
        File file2 =
                new File(inputMessage.getBody().getWorkDirectory() + "file2");
        file2.createNewFile();
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
                procExecutorSrv, procCompletionSrv, procExecutor);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs();
        // Check step 4
        verify(outputProcessor, times(1)).processOutput();
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        verify(appStatus, times(1)).setWaiting();
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
                procExecutorSrv, procCompletionSrv, procExecutor);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(0)).processInputs();
        // Check step 4
        verify(outputProcessor, times(1)).processOutput();
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        verify(appStatus, times(1)).setWaiting();
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
                procExecutorSrv, procCompletionSrv, procExecutor);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs();
        // Check step 4
        verify(outputProcessor, times(0)).processOutput();
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        verify(appStatus, times(1)).setWaiting();
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
                procExecutorSrv, procCompletionSrv, procExecutor);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs();
        // Check step 4
        verify(outputProcessor, times(1)).processOutput();
        // Check step 5
        assertTrue(workingDir.exists());
        // Check step 6
        verify(appStatus, times(1)).setWaiting();
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
    @Test
    public void testCallWhenException() throws Exception {
        mockAllStep(true);

        processor.processJob(inputMessage, inputDownloader, outputProcessor,
                procExecutorSrv, procCompletionSrv, procExecutor);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs();
        // Check status set to error
        verify(appStatus, times(1)).setError();
        // Check step 4
        verify(outputProcessor, never()).processOutput();
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        verify(appStatus, times(1)).setWaiting();
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(1)).getTmProcStopS();

    }
}
