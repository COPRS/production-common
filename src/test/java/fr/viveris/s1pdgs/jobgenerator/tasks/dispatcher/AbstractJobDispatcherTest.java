package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

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
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMaxNumberTaskTablesReachException;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

public class AbstractJobDispatcherTest {

    /**
     * Job generator factory
     */
    @Mock
    private JobsGeneratorFactory jobsGeneratorFactory;

    /**
     * Job generator settings
     */
    @Mock
    private JobGeneratorSettings jobGeneratorSettings;

    @Mock
    private ProcessSettings processSettings;

    /**
     * Job generator task scheduler
     */
    @Mock
    private ThreadPoolTaskScheduler jobGenerationTaskScheduler;

    @Mock
    private AbstractAppCatalogJobService<String> appDataService;

    /**
     * Test set up
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockProcessSettings();
        this.mockAppDataService();
    }

    /**
     * Mock the JobGeneratorSettings
     * 
     * @param maxNbTasktable
     * @param taskTablesDirectory
     */
    private void mockJobGeneratorSettings(int maxNbTasktable,
            String taskTablesDirectory) {
        // Mock the job generator settings
        doAnswer(i -> {
            return taskTablesDirectory;
        }).when(jobGeneratorSettings).getDiroftasktables();
        doAnswer(i -> {
            return maxNbTasktable;
        }).when(jobGeneratorSettings).getMaxnboftasktable();
        doAnswer(i -> {
            return 2000;
        }).when(jobGeneratorSettings).getJobgenfixedrate();
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
        AppDataJobDto<String> appData1 = new AppDataJobDto<>();
        appData1.setIdentifier(12);
        AppDataJobDto<String> appData2 = new AppDataJobDto<>();
        appData2.setIdentifier(12);
        AppDataJobDto<String> appData3 = new AppDataJobDto<>();
        appData3.setIdentifier(12);
        doReturn(Arrays.asList(appData1, appData2, appData3))
                .when(appDataService)
                .findByPodAndState(Mockito.anyString(), Mockito.any());

        doReturn(null).when(appDataService).patchJob(Mockito.anyLong(),
                Mockito.any());
    }

    /**
     * Construct a dispatcher
     * 
     * @return
     */
    private AbstractJobsDispatcherImpl createDispatcher() {
        return new AbstractJobsDispatcherImpl(jobGeneratorSettings,
                processSettings, jobsGeneratorFactory,
                jobGenerationTaskScheduler, appDataService);
    }

    /**
     * Test the constructor
     */
    @Test
    public void testConstructor() {
        this.mockJobGeneratorSettings(4, "./test/data/l0_config/task_tables/");
        AbstractJobsDispatcherImpl dispatcher = this.createDispatcher();
        assertTrue(dispatcher.generators.isEmpty());
    }

    /**
     * Test that if maximal number of task table is reached the initialization
     * failed
     */
    @Test
    public void testNbMaxTaskTables() {
        this.mockJobGeneratorSettings(1, "./test/data");
        AbstractJobsDispatcherImpl dispatcher = this.createDispatcher();
        try {
            dispatcher.initTaskTables();
            fail("An exception shall be raised");
        } catch (JobGenMaxNumberTaskTablesReachException e) {
            assertTrue(e.getMessage().contains("Too much task"));
        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    /**
     * Test the initialize function TODO add several tasktable in data test
     */
    @Test
    public void testInitialize() {

        // Mocks
        this.mockJobGeneratorSettings(4,
                "./test/data/generic_config/task_tables/");
        doAnswer(i -> {
            return null;
        }).when(jobGenerationTaskScheduler).scheduleAtFixedRate(Mockito.any(),
                Mockito.any());

        // Initialize
        AbstractJobsDispatcherImpl dispatcher = this.createDispatcher();
        try {
            dispatcher.initTaskTables();
            verify(jobGenerationTaskScheduler, times(2))
                    .scheduleWithFixedDelay(any(), anyLong());
            verify(jobGenerationTaskScheduler, times(2))
                    .scheduleWithFixedDelay(any(), eq(2000L));

            assertTrue(dispatcher.generators.size() == 2);
            assertTrue(dispatcher.generators.containsKey("TaskTable.AIOP.xml"));
            assertTrue(
                    dispatcher.generators.containsKey("IW_RAW__0_GRDH_1.xml"));

            assertTrue(dispatcher.getCounter() == 2);
        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

}

class AbstractJobsDispatcherImpl extends AbstractJobsDispatcher<String> {

    private int counter;

    public AbstractJobsDispatcherImpl(JobGeneratorSettings taskTablesSettings,
            final ProcessSettings processSettings,
            JobsGeneratorFactory jobsGeneratorFactory,
            ThreadPoolTaskScheduler jobGenerationTaskScheduler,
            final AbstractAppCatalogJobService<String> appDataService) {
        super(taskTablesSettings, processSettings, jobsGeneratorFactory,
                jobGenerationTaskScheduler, appDataService);
        this.counter = 0;
    }

    @Override
    protected AbstractJobsGenerator<String> createJobGenerator(File xmlFile)
            throws JobGenBuildTaskTableException {
        this.counter++;
        return null;
    }

    @Override
    public List<String> getTaskTables(final AppDataJobDto<String> job)
            throws AbstractCodedException {
        return null;
    }

    public int getCounter() {
        return this.counter;
    }
}