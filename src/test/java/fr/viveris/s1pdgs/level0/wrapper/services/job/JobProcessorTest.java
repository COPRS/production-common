package fr.viveris.s1pdgs.level0.wrapper.services.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

import fr.viveris.s1pdgs.level0.wrapper.TestUtils;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ProcessTimeoutException;
import fr.viveris.s1pdgs.level0.wrapper.services.file.InputDownloader;
import fr.viveris.s1pdgs.level0.wrapper.services.file.OutputProcessor;
import fr.viveris.s1pdgs.level0.wrapper.services.task.PoolExecutorCallable;
import fr.viveris.s1pdgs.level0.wrapper.test.MockPropertiesTest;

/**
 * Test the job processor
 * 
 * @author Viveris Technologies
 */
public class JobProcessorTest extends MockPropertiesTest {

    /**
     * Input downloader
     */
    @Mock
    private InputDownloader inputDownloader;

    /**
     * Output processsor
     */
    @Mock
    private OutputProcessor outputProcessor;

    /**
     * Level processes executor
     */
    @Mock
    private PoolExecutorCallable procExecutor;

    /**
     * Registry of kafka consumers
     */
    @Mock
    private KafkaListenerEndpointRegistry kafkaRegistry;
    @Mock
    private MessageListenerContainer kafkaContainer;

    /**
     * Job to process
     */
    private JobDto job;

    /**
     * Processor to test
     */
    private JobProcessor processor;

    /**
     * Working directory
     */
    private File workingDir;

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

        job = TestUtils.buildL0JobDto();
        workingDir = new File(job.getWorkDirectory());
        if (!workingDir.exists()) {
            workingDir.mkdir();
        }
        processor = new JobProcessor(job, appStatus, properties, devProperties,
                "kafka-id", kafkaRegistry, inputDownloader, outputProcessor,
                procExecutor);
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
     * Test the method for getting prefix monitor logs
     */
    @Test
    public void testGetPrefixMonitorLogs() {
        String baseLog = "[MONITOR]";
        String productLog =
                "[productName SESSIONID] [workDir ./test_work_dir/]";

        assertEquals(baseLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_DFT));
        assertEquals(baseLog, processor.getPrefixMonitorLog("tutu"));

        assertEquals(productLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_ERROR));

        assertEquals(baseLog + " [step 3] " + productLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_PROCESS));
        assertEquals(baseLog + " [step 2] " + productLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_INPUT));
        assertEquals(baseLog + " [step 4] " + productLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_OUTPUT));
        assertEquals(baseLog + " [step 5] " + productLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_ERASE));
        assertEquals(baseLog + " [step 7] " + productLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_RESUME));
        assertEquals(baseLog + " [step 6] " + productLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_STATUS));
        assertEquals(baseLog + " [step 0] " + productLog,
                processor.getPrefixMonitorLog(JobProcessor.LOG_END));

    }

    /**
     * Test processPoolProcesses
     * 
     * @throws AbstractCodedException
     * @throws InterruptedException
     */
    @Test
    public void testprocessPoolProcesses()
            throws AbstractCodedException, InterruptedException {
        doReturn(true).when(procExecutor).call();
        processor.processPoolProcesses();
        Thread.sleep(500);
        verify(procExecutor, times(1)).call();
        processor.waitForPoolProcessesEnding();
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
        doThrow(new ProcessTimeoutException("timeout exception"))
                .when(procExecutor).call();
        processor.processPoolProcesses();
        Thread.sleep(500);

        thrown.expect(ProcessTimeoutException.class);
        thrown.expectMessage("timeout exception");
        processor.waitForPoolProcessesEnding();
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
        processor.processPoolProcesses();
        Thread.sleep(500);

        thrown.expect(InternalErrorException.class);
        processor.waitForPoolProcessesEnding();
    }

    /**
     * Test terminateProcessesExecution
     * 
     * @throws InterruptedException
     */
    @Test
    public void testterminateProcessesExecution() throws InterruptedException {
        processor.processPoolProcesses();
        Thread.sleep(500);
        processor.terminateProcessesExecution();
        verify(properties, times(1)).getTmProcStopS();
    }

    /**
     * Test process inputs
     * 
     * @throws AbstractCodedException
     * @throws InterruptedException
     */
    @Test
    public void testProcessInputs()
            throws AbstractCodedException, InterruptedException {
        doNothing().when(inputDownloader).processInputs();

        processor.processInputs();
        verify(inputDownloader, only()).processInputs();
    }

    /**
     * Test process outputs
     * 
     * @throws AbstractCodedException
     * @throws InterruptedException
     */
    @Test
    public void testProcessOutputs()
            throws AbstractCodedException, InterruptedException {
        doNothing().when(outputProcessor).processOutput();

        processor.processOutputs();
        verify(outputProcessor, only()).processOutput();
    }

    /**
     * Test erase local directory
     * 
     * @throws IOException
     */
    @Test
    public void testEraseLocalDirectory() throws IOException {
        File folder1 = new File(job.getWorkDirectory() + "folder1");
        folder1.mkdir();
        File file1 = new File(
                job.getWorkDirectory() + "folder1" + File.separator + "file1");
        file1.createNewFile();
        File file2 = new File(job.getWorkDirectory() + "file2");
        file2.createNewFile();
        assertTrue(workingDir.exists());
        assertTrue(file1.exists());
        processor.eraseLocalDirectory();
        assertFalse(workingDir.exists());
    }

    /**
     * Test resume consumer when ok
     */
    @Test
    public void testResumeConsumer() {
        doReturn(kafkaContainer).when(kafkaRegistry)
                .getListenerContainer(Mockito.eq("kafka-id"));
        doNothing().when(kafkaContainer).resume();

        processor.resumeConsumer();
        verify(kafkaRegistry, atLeast(1))
                .getListenerContainer(Mockito.eq("kafka-id"));
        verify(kafkaContainer, times(1)).resume();
    }

    /**
     * Test resume consumer when no consumer
     */
    @Test
    public void testResumeConsumerWhenNoConsumer() {
        doReturn(null).when(kafkaRegistry)
                .getListenerContainer(Mockito.eq("kafka-id"));
        doNothing().when(kafkaContainer).resume();

        processor.resumeConsumer();
        verify(kafkaRegistry, atLeast(1))
                .getListenerContainer(Mockito.eq("kafka-id"));
        verifyZeroInteractions(kafkaContainer);
    }

    @Test
    public void testStatus() {

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
            doThrow(new ProcessTimeoutException("timeout exception"))
                    .when(procExecutor).call();
        } else {
            doReturn(true).when(procExecutor).call();
        }
        // Step 2
        doNothing().when(inputDownloader).processInputs();
        // Step 4
        doNothing().when(outputProcessor).processOutput();
        // Step 5
        File folder1 = new File(job.getWorkDirectory() + "folder1");
        folder1.mkdir();
        File file1 = new File(
                job.getWorkDirectory() + "folder1" + File.separator + "file1");
        file1.createNewFile();
        File file2 = new File(job.getWorkDirectory() + "file2");
        file2.createNewFile();
        // Step 7
        doReturn(kafkaContainer).when(kafkaRegistry)
                .getListenerContainer(Mockito.eq("kafka-id"));
        doNothing().when(kafkaContainer).resume();
    }

    /**
     * Nominal test case of call
     * 
     * @throws Exception
     */
    @Test
    public void testCall() throws Exception {
        mockAllStep(false);

        processor.call();

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
        // Check step 7
        verify(kafkaRegistry, atLeast(1))
                .getListenerContainer(Mockito.eq("kafka-id"));
        verify(kafkaContainer, times(1)).resume();
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

        processor.call();

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
        // Check step 7
        verify(kafkaRegistry, atLeast(1))
                .getListenerContainer(Mockito.eq("kafka-id"));
        verify(kafkaContainer, times(1)).resume();
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

        processor.call();

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
        // Check step 7
        verify(kafkaRegistry, atLeast(1))
                .getListenerContainer(Mockito.eq("kafka-id"));
        verify(kafkaContainer, times(1)).resume();
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(0)).getTmProcStopS();
    }

    /**
     * Nominal test case of call when processes execution is deactivated
     * 
     * @throws Exception
     */
    @Test
    public void testCallStepProcessNotActive() throws Exception {
        mockAllStep(false);
        mockDevProperties(true, false, true, true);

        processor.call();

        // Check step 3
        verify(procExecutor, times(0)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs();
        // Check step 4
        verify(outputProcessor, times(1)).processOutput();
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        verify(appStatus, times(1)).setWaiting();
        // Check step 7
        verify(kafkaRegistry, atLeast(1))
                .getListenerContainer(Mockito.eq("kafka-id"));
        verify(kafkaContainer, times(1)).resume();
        // Check properties call
        verify(properties, times(0)).getTmProcAllTasksS();
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

        processor.call();

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
        // Check step 7
        verify(kafkaRegistry, atLeast(1))
                .getListenerContainer(Mockito.eq("kafka-id"));
        verify(kafkaContainer, times(1)).resume();
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(0)).getTmProcStopS();

        // REexcute erase to purge test folder
        processor.eraseLocalDirectory();
    }

    /**
     * Test call when an exception during processes execution
     * 
     * @throws Exception
     */
    @Test
    public void testCallWhenException() throws Exception {
        mockAllStep(true);

        processor.call();

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
        // Check step 7
        verify(kafkaRegistry, atLeast(1))
                .getListenerContainer(Mockito.eq("kafka-id"));
        verify(kafkaContainer, times(1)).resume();
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(1)).getTmProcStopS();

    }
}
