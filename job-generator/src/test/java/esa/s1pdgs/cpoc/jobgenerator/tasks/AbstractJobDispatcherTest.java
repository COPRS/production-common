package esa.s1pdgs.cpoc.jobgenerator.tasks;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

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

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchApiError;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMaxNumberTaskTablesReachException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.JobsGeneratorFactory;

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
    private AppCatalogJobClient appDataService;

    private AbstractJobsDispatcherImpl testDispatcher;

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

        this.mockJobGeneratorSettings(4,
                "./test/data/generic_config/task_tables/");
        doAnswer(i -> {
            return null;
        }).when(jobGenerationTaskScheduler).scheduleAtFixedRate(Mockito.any(),
                Mockito.any());

        testDispatcher = createDispatcher();
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
        AppDataJobDto appData1 = new AppDataJobDto();
        appData1.setIdentifier(12);
        appData1.setProduct(new AppDataJobProductDto());
        appData1.getProduct().setProductName("p1");
        AppDataJobDto appData2 = new AppDataJobDto();
        appData2.setIdentifier(12);
        appData2.setProduct(new AppDataJobProductDto());
        appData2.getProduct().setProductName("p2");
        AppDataJobDto appData3 = new AppDataJobDto();
        appData3.setIdentifier(12);
        appData3.setProduct(new AppDataJobProductDto());
        appData3.getProduct().setProductName("p3");
        doReturn(Arrays.asList(appData1, appData2, appData3))
                .when(appDataService)
                .findByPodAndState(Mockito.anyString(), Mockito.any());

        doReturn(null).when(appDataService).patchJob(Mockito.anyLong(),
                Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());

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
            verify(jobGenerationTaskScheduler, times(3))
                    .scheduleWithFixedDelay(any(), anyLong());
            verify(jobGenerationTaskScheduler, times(3))
                    .scheduleWithFixedDelay(any(), eq(2000L));

            assertTrue(dispatcher.generators.size() == 3);
            assertTrue(dispatcher.generators.containsKey("TaskTable.AIOP.xml"));
            assertTrue(dispatcher.generators.containsKey("IW_RAW__0_GRDH_1.xml"));

            assertTrue(dispatcher.getCounter() == 3);
        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    /**
     * Test the initialize function TODO add several tasktable in data test
     */
    @Test
    public void testInitializeInModeProd() {

        // Mocks
        this.mockJobGeneratorSettings(4,
                "./test/data/generic_config/task_tables/");
        doAnswer(i -> {
            return null;
        }).when(jobGenerationTaskScheduler).scheduleAtFixedRate(Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(i -> {
            return ApplicationMode.PROD;
        }).when(processSettings).getMode();

        // Initialize
        AbstractJobsDispatcherImpl dispatcher = this.createDispatcher();
        try {
            dispatcher.initTaskTables();
            verify(jobGenerationTaskScheduler, times(3))
                    .scheduleWithFixedDelay(any(), anyLong());
            verify(jobGenerationTaskScheduler, times(3))
                    .scheduleWithFixedDelay(any(), eq(2000L));

            assertTrue(dispatcher.generators.size() == 3);
            assertTrue(dispatcher.generators.containsKey("TaskTable.AIOP.xml"));
            assertTrue(
                    dispatcher.generators.containsKey("IW_RAW__0_GRDH_1.xml"));

            assertTrue(dispatcher.getCounter() == 3);

            verify(appDataService, times(1)).findByPodAndState(
                    Mockito.eq("hostname"),
                    Mockito.eq(AppDataJobDtoState.GENERATING));

            assertTrue(dispatcher.getCounterDispatch() == 3);

        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    @Test
    public void testDispatchNewJob() throws AbstractCodedException {

        doReturn(null).when(appDataService).patchJob(Mockito.anyLong(),
                Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());

        AppDataJobDto dto = new AppDataJobDto();
        dto.setIdentifier(12);
        dto.setProduct(new AppDataJobProductDto());
        dto.getProduct().setProductName("p1");

        AppDataJobDto expected = new AppDataJobDto();
        expected.setIdentifier(12);
        expected.setProduct(new AppDataJobProductDto());
        expected.getProduct().setProductName("p1");
        expected.setState(AppDataJobDtoState.GENERATING);
        expected.getGenerations().add(new AppDataJobGenerationDto());
        expected.getGenerations().get(0).setTaskTable("tt1");
        expected.getGenerations().add(new AppDataJobGenerationDto());
        expected.getGenerations().get(1).setTaskTable("tt2");

        testDispatcher.dispatch(dto);
        verify(appDataService, times(1)).patchJob(Mockito.eq(12L), Mockito.any(),
                Mockito.eq(false), Mockito.eq(false), Mockito.eq(true));

    }

    @Test
    public void testDispatchJobNoModification() throws AbstractCodedException {

        doReturn(null).when(appDataService).patchJob(Mockito.anyLong(),
                Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());

        AppDataJobDto dto = new AppDataJobDto();
        dto.setIdentifier(12);
        dto.setProduct(new AppDataJobProductDto());
        dto.getProduct().setProductName("p1");
        dto.setState(AppDataJobDtoState.GENERATING);
        dto.getGenerations().add(new AppDataJobGenerationDto());
        dto.getGenerations().get(0).setTaskTable("tt1");
        dto.getGenerations().add(new AppDataJobGenerationDto());
        dto.getGenerations().get(1).setTaskTable("tt2");
        dto.getGenerations().get(1).setState(AppDataJobGenerationDtoState.READY);

        AppDataJobDto expected = new AppDataJobDto();
        expected.setIdentifier(12);
        expected.setProduct(new AppDataJobProductDto());
        expected.getProduct().setProductName("p1");
        expected.setState(AppDataJobDtoState.GENERATING);
        expected.getGenerations().add(new AppDataJobGenerationDto());
        expected.getGenerations().get(0).setTaskTable("tt1");
        expected.getGenerations().add(new AppDataJobGenerationDto());
        expected.getGenerations().get(1).setTaskTable("tt2");
        expected.getGenerations().get(1).setState(AppDataJobGenerationDtoState.READY);

        testDispatcher.dispatch(dto);
        verifyZeroInteractions(appDataService);

    }

    @Test(expected = AppCatalogJobPatchApiError.class)
    public void testDispatchJobException() throws AbstractCodedException {

        doThrow(new AppCatalogJobPatchApiError("uri", "body", "message")).when(appDataService).patchJob(Mockito.anyLong(),
                Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());

        AppDataJobDto dto = new AppDataJobDto();
        dto.setIdentifier(12);
        dto.setProduct(new AppDataJobProductDto());
        dto.getProduct().setProductName("p1");

        testDispatcher.dispatch(dto);

    }

    @Test
    public void testDispatchJobModification() throws AbstractCodedException {

        doReturn(null).when(appDataService).patchJob(Mockito.anyLong(),
                Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());

        AppDataJobDto dto = new AppDataJobDto();
        dto.setIdentifier(12);
        dto.setProduct(new AppDataJobProductDto());
        dto.getProduct().setProductName("p1");
        dto.setState(AppDataJobDtoState.GENERATING);
        dto.getGenerations().add(new AppDataJobGenerationDto());
        dto.getGenerations().get(0).setTaskTable("tt3");
        dto.getGenerations().add(new AppDataJobGenerationDto());
        dto.getGenerations().get(1).setTaskTable("tt1");
        dto.getGenerations().get(1).setState(AppDataJobGenerationDtoState.READY);

        AppDataJobDto expected = new AppDataJobDto();
        expected.setIdentifier(12);
        expected.setProduct(new AppDataJobProductDto());
        expected.getProduct().setProductName("p1");
        expected.setState(AppDataJobDtoState.GENERATING);
        expected.getGenerations().add(new AppDataJobGenerationDto());
        expected.getGenerations().get(0).setTaskTable("tt1");
        expected.getGenerations().get(0).setState(AppDataJobGenerationDtoState.READY);
        expected.getGenerations().add(new AppDataJobGenerationDto());
        expected.getGenerations().get(1).setTaskTable("tt2");

        testDispatcher.dispatch(dto);
        verify(appDataService, times(1)).patchJob(Mockito.eq(12L), Mockito.any(),
                Mockito.eq(false), Mockito.eq(false), Mockito.eq(true));

    }

}

class AbstractJobsDispatcherImpl extends AbstractJobsDispatcher<String> {

    private int counter;
    private int counterDispatch;

    public AbstractJobsDispatcherImpl(JobGeneratorSettings taskTablesSettings,
            final ProcessSettings processSettings,
            JobsGeneratorFactory jobsGeneratorFactory,
            ThreadPoolTaskScheduler jobGenerationTaskScheduler,
            final AppCatalogJobClient appDataService) {
        super(taskTablesSettings, processSettings, jobsGeneratorFactory,
                jobGenerationTaskScheduler, appDataService);
        this.counter = 0;
        this.counterDispatch = 0;
    }

    @Override
    protected AbstractJobsGenerator<String> createJobGenerator(File xmlFile)
            throws JobGenBuildTaskTableException {
        this.counter++;
        return null;
    }

    public void dispatch(final AppDataJobDto job)
            throws AbstractCodedException {
        counterDispatch++;
        super.dispatch(job);
    }

    @Override
    public List<String> getTaskTables(final AppDataJobDto job)
            throws AbstractCodedException {
        return Arrays.asList("tt1", "tt2");
    }

    public int getCounter() {
        return this.counter;
    }

    public int getCounterDispatch() {
        return counterDispatch;
    }

    @Override
    protected String getTaskForFunctionalLog() {
        return "Task";
    }
}