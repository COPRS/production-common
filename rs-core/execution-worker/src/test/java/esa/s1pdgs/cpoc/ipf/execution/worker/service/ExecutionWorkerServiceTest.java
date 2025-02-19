/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ipf.execution.worker.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfExecutionWorkerProcessTimeoutException;
import esa.s1pdgs.cpoc.ipf.execution.worker.TestUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.InputDownloader;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.OutputEstimation;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.OutputProcessor;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.ipf.execution.worker.test.MockPropertiesTest;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

/**
 * Test the job processor
 * 
 * @author Viveris Technologies
 */
public class ExecutionWorkerServiceTest extends MockPropertiesTest {

	@Mock
	private CommonConfigurationProperties commonProperties;
	
    /**
     * Output processsor
     */
    @Mock
    private ObsClient obsClient;

    /**
     * Job to process
     */
    private IpfExecutionJob inputMessage;

    /**
     * Processor to test
     */
    private ExecutionWorkerService processor;

    /**
     * Working directory
     */
    private File workingDir;

    @Mock
    private InputDownloader inputDownloader;

    @Mock
    private OutputProcessor outputProcessor;
    
    @Mock
    private OutputEstimation outputEstimation;

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
    
    private final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("TestOutputHandling");

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
        //devProperties.getStepsActivation().put("erasing", Boolean.FALSE);

        inputMessage = TestUtils.buildL0IpfExecutionJob();
        workingDir = new File(inputMessage.getWorkDirectory());
        if (!workingDir.exists()) {
            workingDir.mkdir();
        }
        mockWorkingdirProperties(workingDir.toPath());
        processor = new ExecutionWorkerService(commonProperties, appStatus, properties, devProperties,
                obsClient);
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
        final Future<?> fut = procCompletionSrvTmp.submit(procExecutor);

        thrown.expect(IpfExecutionWorkerProcessTimeoutException.class);
        thrown.expectMessage("timeout exception");
        processor.waitForPoolProcessesEnding("test", fut, procCompletionSrvTmp, 1000L);
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
        final Future<?> fut = procCompletionSrvTmp.submit(procExecutor);

        thrown.expect(InternalErrorException.class);
        processor.waitForPoolProcessesEnding("test", fut, procCompletionSrvTmp, 1000L);
    }

    @Test
    public void testCleanJobProcessing() throws IOException {
        final File folder1 =
                new File(inputMessage.getWorkDirectory() + "folder1");
        folder1.mkdir();
        final File file1 = new File(inputMessage.getWorkDirectory()
                + "folder1" + File.separator + "file1");
        file1.createNewFile();
        final File file2 =
                new File(inputMessage.getWorkDirectory() + "file2");
        file2.createNewFile();
        assertTrue(workingDir.exists());
        assertTrue(file1.exists());      
        processor.cleanJobProcessing(inputMessage, true, procExecutorSrv);

        verify(properties, times(1)).getTmProcStopS();
        assertTrue(workingDir.exists());
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
        doReturn(Collections.emptyList()).when(inputDownloader).processInputs(reporting);
        // Step 4
        doReturn(Collections.emptyList()).when(outputProcessor).processOutput(reporting, UUID.randomUUID(), new IpfExecutionJob());
        // Step 5
        final File folder1 =
                new File(inputMessage.getWorkDirectory() + "folder1");
        folder1.mkdir();
        final File file1 = new File(inputMessage.getWorkDirectory()
                + "folder1" + File.separator + "file1");
        file1.createNewFile();
        final File file2 =
                new File(inputMessage.getWorkDirectory() + "file2");
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
        processor.apply(inputMessage);
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
        
        processor.processJob(inputMessage, inputDownloader, outputProcessor, outputEstimation,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs(reporting);
        // Check step 4
        verify(outputProcessor, times(1)).processOutput(
        		Mockito.eq(reporting), 
        		Mockito.eq(reporting.getUid()),
        		Mockito.any()
        );
       
        // Check step 5
        assertTrue(workingDir.exists());
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

        processor.processJob(inputMessage, inputDownloader, outputProcessor, outputEstimation,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(0)).processInputs(reporting);
        // Check step 4
        verify(outputProcessor, times(1)).processOutput(
        		Mockito.eq(reporting), 
        		Mockito.eq(reporting.getUid()),
        		Mockito.any()
        );
        // Check step 5
        assertTrue(workingDir.exists());
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

        processor.processJob(inputMessage, inputDownloader, outputProcessor, outputEstimation,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs(reporting);
        // Check step 4
        verify(outputProcessor, times(0)).processOutput(reporting, UUID.randomUUID(), new IpfExecutionJob());
        // Check step 5
        assertTrue(workingDir.exists());
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

        processor.processJob(inputMessage, inputDownloader, outputProcessor, outputEstimation,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs(reporting);
        // Check step 4
        verify(outputProcessor, times(1)).processOutput(
        		Mockito.eq(reporting), 
        		Mockito.eq(reporting.getUid()),
        		Mockito.any()
        );
        // Check step 5
        assertTrue(workingDir.exists());
        // Check step 6
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(0)).getTmProcStopS();

        // REexcute erase to purge test folder
        processor.cleanJobProcessing(inputMessage, false,
                procExecutorSrv);
    }

    /**
     * Test call when an exception during processes execution
     * 
     * @throws Exception
     */
    public void testCallWhenException() throws Exception {
        mockAllStep(true);

        processor.processJob(inputMessage, inputDownloader, outputProcessor, outputEstimation,
                procExecutorSrv, procCompletionSrv, procExecutor, reporting);

        // Check step 3
        verify(procExecutor, times(1)).call();
        // Check step 2
        verify(inputDownloader, times(1)).processInputs(reporting);
        // Check status set to error
        verify(appStatus, times(1)).setError("PROCESSING");
        // Check step 4
        verify(outputProcessor, never()).processOutput(reporting, UUID.randomUUID(), new IpfExecutionJob());
        // Check step 5
        assertFalse(workingDir.exists());
        // Check step 6
        // Check properties call
        verify(properties, times(1)).getTmProcAllTasksS();
        verify(properties, times(1)).getTmProcStopS();

    }
    
    
    private final Callable<Void> newSleepCallable(final long msSleep) {
    	return new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Thread.sleep(msSleep);
				System.err.println("Finished");
				return null;
			}
    		
		};
    }
    
    @Test
    public void testWaitForPoolProcessesEnding_Success() throws Exception {
		final ExecutorService exec = Executors.newSingleThreadExecutor();
    	try {
    		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(exec);
    		final Future<?> submittedFuture = procCompletionSrv.submit(newSleepCallable(10L));
        	processor.waitForPoolProcessesEnding("test", submittedFuture, procCompletionSrv, 1000L);
    	}
    	finally {
    		exec.shutdownNow();
    	}
    }
    
    @Test(expected=InternalErrorException.class)
    public void testWaitForPoolProcessesEnding_OnTimeout_ShallThrowException() throws Exception {
		final ExecutorService exec = Executors.newSingleThreadExecutor();
    	try {
    		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(exec);
    		final Future<?> submittedFuture = procCompletionSrv.submit(newSleepCallable(10000L));
        	processor.waitForPoolProcessesEnding("test", submittedFuture, procCompletionSrv, 10L);
        	fail();
    	}
    	finally {
    		exec.shutdownNow();
    	}
    }
}
