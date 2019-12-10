package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.edrssession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.edrssession.L0AppJobDispatcher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.edrssession.L0AppJobsGenerator;
import esa.s1pdgs.cpoc.ipf.preparation.worker.utils.TestL0Utils;

/**
 * Test the class JobDispatcher
 * 
 * @author Cyrielle Gailliard
 */
public class L0AppJobDispatcherTest {

    /**
     * Job generator factory
     */
    @Mock
    private JobsGeneratorFactory jobsGeneratorFactory;

    /**
     * Job generator settings
     */
    @Mock
    private IpfPreparationWorkerSettings ipfPreparationWorkerSettings;

    @Mock
    private ProcessSettings processSettings;

    /**
     * Job generator task scheduler
     */
    @Mock
    private ThreadPoolTaskScheduler jobGenerationTaskScheduler;

    @Mock
    private L0AppJobsGenerator mockGenerator;

    @Mock
    private AppCatalogJobClient appDataService;

    /**
     * Test set up
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // Mock process settings
        this.mockJobGeneratorSettings();
        this.mockProcessSettings();

        // Mock app catalog service
        this.mockAppDataService();

        // Mcok
        doAnswer(i -> {
            return mockGenerator;
        }).when(jobsGeneratorFactory).createJobGeneratorForEdrsSession(any(),
                any());
        doAnswer(i -> {
            return null;
        }).when(jobGenerationTaskScheduler).scheduleAtFixedRate(any(), any());
    }

    /**
     * Construct a dispatcher
     * 
     * @return
     */
    private L0AppJobDispatcher createSessionDispatcher() {
        return new L0AppJobDispatcher(ipfPreparationWorkerSettings,
                processSettings, jobsGeneratorFactory,
                jobGenerationTaskScheduler, appDataService);
    }

    /**
     * Mock the JobGeneratorSettings
     */
    private void mockJobGeneratorSettings() {
        // Mock the job generator settings
        doAnswer(i -> {
            return "./test/data/l0_config/task_tables/";
        }).when(ipfPreparationWorkerSettings).getDiroftasktables();
        doAnswer(i -> {
            return 4;
        }).when(ipfPreparationWorkerSettings).getMaxnboftasktable();
        doAnswer(i -> {
            return 2000;
        }).when(ipfPreparationWorkerSettings).getJobgenfixedrate();
    }

    private void mockProcessSettings() {
        Mockito.doAnswer(i -> {
            Map<String, String> r = new HashMap<String, String>(2);
            return r;
        }).when(processSettings).getParams();
        Mockito.doAnswer(i -> {
            Map<String, String> r = new HashMap<String, String>(5);
            r.put("SM_RAW__0S", "^S1[A-B]_S[1-6]_RAW__0S.*$");
            r.put("AN_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
            r.put("ZS_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
            r.put("REP_L0PSA_", "^S1[A|B|_]_OPER_REP_ACQ.*$");
            r.put("REP_EFEP_", "^S1[A|B|_]_OPER_REP_PASS.*.EOF$");
            return r;
        }).when(processSettings).getOutputregexps();
        Mockito.doAnswer(i -> {
            return ApplicationLevel.L0;
        }).when(processSettings).getLevel();
        Mockito.doAnswer(i -> {
            return "hostname";
        }).when(processSettings).getHostname();
        Mockito.doAnswer(i -> {
            return ApplicationMode.TEST;
        }).when(processSettings).getMode();
    }

    private void mockAppDataService()
            throws InternalErrorException, AbstractCodedException {
        doReturn(Arrays.asList(TestL0Utils.buildAppDataEdrsSession(true)))
                .when(appDataService)
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.anyString(), Mockito.anyString());
        AppDataJob<?> primaryCheckAppJob =
                TestL0Utils.buildAppDataEdrsSession(false);
        primaryCheckAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationState.PRIMARY_CHECK);
        AppDataJob<?> readyAppJob =
                TestL0Utils.buildAppDataEdrsSession(false);
        readyAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationState.READY);
        AppDataJob<?> sentAppJob =
                TestL0Utils.buildAppDataEdrsSession(false);
        sentAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationState.SENT);
        doReturn(TestL0Utils.buildAppDataEdrsSession(false))
                .when(appDataService).patchJob(Mockito.eq(123L), Mockito.any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean());
        doReturn(primaryCheckAppJob).when(appDataService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("TaskTable.AIOP.xml"),
                Mockito.eq(AppDataJobGenerationState.PRIMARY_CHECK));
        doReturn(readyAppJob).when(appDataService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("TaskTable.AIOP.xml"),
                Mockito.eq(AppDataJobGenerationState.READY));
        doReturn(sentAppJob).when(appDataService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("TaskTable.AIOP.xml"),
                Mockito.eq(AppDataJobGenerationState.SENT));
    }

    @Test
    public void testCreate() {
        File taskTable1 = new File(
                "./test/data/l0_config/task_tables/TaskTable.AIOP.xml");

        // Initialize
        L0AppJobDispatcher dispatcher = this.createSessionDispatcher();
        try {
            dispatcher.createJobGenerator(taskTable1);
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForEdrsSession(any(), any());
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForEdrsSession(eq(taskTable1), any());
        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    /**
     * Test the initialize function
     */
    @Test
    public void testInitialize() {
        File taskTable1 = new File(
                "./test/data/l0_config/task_tables/TaskTable.AIOP.xml");

        // Intitialize
        L0AppJobDispatcher dispatcher = this.createSessionDispatcher();
        try {
            dispatcher.initialize();
            verify(jobGenerationTaskScheduler, times(1))
                    .scheduleWithFixedDelay(any(), anyLong());
            verify(jobGenerationTaskScheduler, times(1))
                    .scheduleWithFixedDelay(any(), eq(2000L));
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForEdrsSession(any(), any());
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForEdrsSession(eq(taskTable1), any());

            assertTrue(dispatcher.getGenerators().size() == 1);
            assertTrue(dispatcher.getGenerators().containsKey(taskTable1.getName()));
        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    /**
     * Test dispatch
     */
    @Test
    public void testGetTaskTable() {

        AppDataJob appData =
                TestL0Utils.buildAppDataEdrsSession(false);

        // Init dispatcher
        L0AppJobDispatcher dispatcher = this.createSessionDispatcher();
        try {
            dispatcher.initialize();
        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }

        // Dispatch
        assertEquals(1, dispatcher.getTaskTables(appData).size());
    }
}