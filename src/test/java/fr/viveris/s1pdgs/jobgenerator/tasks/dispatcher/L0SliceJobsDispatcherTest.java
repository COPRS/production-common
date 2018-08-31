package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.model.l1routing.L1Routing;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.LevelProductsJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.utils.TestL1Utils;

public class L0SliceJobsDispatcherTest {

    /**
     * To test exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    private AbstractAppCatalogJobService<LevelProductDto> appDataService;

    @Mock
    private LevelProductsJobsGenerator mockGeneratorIW;
    @Mock
    private LevelProductsJobsGenerator mockGeneratorOther;

    @Mock
    private XmlConverter xmlConverter;

    private File taskTable1 =
            new File("./test/data/l1_config/task_tables/EN_RAW__0_GRDF_1.xml");
    private File taskTable2 =
            new File("./test/data/l1_config/task_tables/EW_RAW__0_GRDH_1.xml");
    private File taskTable3 =
            new File("./test/data/l1_config/task_tables/EW_RAW__0_GRDM_1.xml");
    private File taskTable4 =
            new File("./test/data/l1_config/task_tables/IW_RAW__0_GRDH_1.xml");
    private int nbTaskTables;

    private L0SliceJobsDispatcher dispatcher;

    /**
     * Test set up
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        // Mocks
        MockitoAnnotations.initMocks(this);

        // Mock process settings
        this.mockProcessSettings();

        // Mock the converter
        L1Routing routing = TestL1Utils.buildL1Routing();
        try {
            Mockito.when(
                    xmlConverter.convertFromXMLToObject(Mockito.anyString()))
                    .thenReturn(routing);
        } catch (IOException | JAXBException e) {
            fail("Exception occurred: " + e.getMessage());
        }

        // Mock the job generator settings
        doAnswer(i -> {
            return "./test/data/l1_config/task_tables/";
        }).when(jobGeneratorSettings).getDiroftasktables();
        doAnswer(i -> {
            return 25;
        }).when(jobGeneratorSettings).getMaxnboftasktable();
        doAnswer(i -> {
            return 2000;
        }).when(jobGeneratorSettings).getJobgenfixedrate();

        // Mock the job generators
        doNothing().when(mockGeneratorIW).run();
        doNothing().when(mockGeneratorOther).run();

        // Mock the job generator factory
        try {
            doAnswer(i -> {
                return null;
            }).when(jobsGeneratorFactory).createJobGeneratorForEdrsSession(
                    Mockito.any(), Mockito.any());
            doAnswer(i -> {
                File f = (File) i.getArgument(0);
                if (f.getName().startsWith("IW")) {
                    return this.mockGeneratorIW;
                }
                return this.mockGeneratorOther;
            }).when(jobsGeneratorFactory)
                    .createJobGeneratorForL0Slice(Mockito.any(), Mockito.any());
        } catch (JobGenBuildTaskTableException e) {
            fail("Exception occurred: " + e.getMessage());
        }

        // Mock the task scheduler
        doAnswer(i -> {
            return null;
        }).when(jobGenerationTaskScheduler)
                .scheduleWithFixedDelay(Mockito.any(), Mockito.anyLong());

        // Retrieve number of tasktables
        File taskTableDirectory = new File("./test/data/l1_config/task_tables");
        if (taskTableDirectory.isDirectory()) {
            String[] files = taskTableDirectory.list();
            if (files != null) {
                this.nbTaskTables = files.length;
            } else {
                this.nbTaskTables = -1;
            }
        } else {
            this.nbTaskTables = -1;
        }

        // Mock app catalog service
        this.mockAppDataService();

        // Return the dispatcher
        this.dispatcher = new L0SliceJobsDispatcher(jobGeneratorSettings,
                processSettings, jobsGeneratorFactory,
                jobGenerationTaskScheduler, xmlConverter,
                "./test/data/l1_config/routing.xml", appDataService);
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
        doReturn(Arrays.asList(TestL1Utils.buildJobGeneration(false)))
                .when(appDataService)
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.anyString(), Mockito.anyString());
        AppDataJobDto<LevelProductDto> primaryCheckAppJob =
                TestL1Utils.buildJobGeneration(true);
        primaryCheckAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationDtoState.PRIMARY_CHECK);
        AppDataJobDto<LevelProductDto> readyAppJob =
                TestL1Utils.buildJobGeneration(true);
        readyAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationDtoState.READY);
        AppDataJobDto<LevelProductDto> sentAppJob =
                TestL1Utils.buildJobGeneration(true);
        sentAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationDtoState.SENT);
        doReturn(TestL1Utils.buildJobGeneration(true)).when(appDataService)
                .patchJob(Mockito.eq(123L), Mockito.any());
        doReturn(primaryCheckAppJob).when(appDataService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationDtoState.PRIMARY_CHECK));
        doReturn(readyAppJob).when(appDataService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationDtoState.READY));
        doReturn(sentAppJob).when(appDataService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationDtoState.SENT));
    }

    @Test
    public void testCreate() {
        try {
            this.dispatcher.createJobGenerator(taskTable1);
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForL0Slice(any(), any());
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForL0Slice(eq(taskTable1), any());
        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    /**
     * Test the initialize function
     */
    @Test
    public void testInitialize() {
        try {
            this.dispatcher.initialize();

            // Verify creation of job generator
            verify(jobGenerationTaskScheduler, times(this.nbTaskTables))
                    .scheduleWithFixedDelay(any(), Mockito.anyLong());
            verify(jobGenerationTaskScheduler, times(this.nbTaskTables))
                    .scheduleWithFixedDelay(any(), eq(2000L));
            verify(jobsGeneratorFactory, never())
                    .createJobGeneratorForEdrsSession(any(), any());
            verify(jobsGeneratorFactory, times(this.nbTaskTables))
                    .createJobGeneratorForL0Slice(any(), any());
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForL0Slice(eq(taskTable1), any());
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForL0Slice(eq(taskTable2), any());
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForL0Slice(eq(taskTable3), any());
            verify(jobsGeneratorFactory, times(1))
                    .createJobGeneratorForL0Slice(eq(taskTable4), any());
            assertTrue(dispatcher.generators.size() == this.nbTaskTables);
            assertTrue(dispatcher.generators.containsKey(taskTable1.getName()));
            assertTrue(dispatcher.generators.containsKey(taskTable2.getName()));
            assertTrue(dispatcher.generators.containsKey(taskTable3.getName()));
            assertTrue(dispatcher.generators.containsKey(taskTable4.getName()));

            // Verify creation of routing creation
            L1Routing routing = TestL1Utils.buildL1Routing();
            assertTrue("Invalid number of routes",
                    routing.getRoutes().size() == dispatcher.routingMap.size());
            routing.getRoutes().forEach(route -> {
                String key = route.getRouteFrom().getAcquisition() + "_"
                        + route.getRouteFrom().getSatelliteId();
                assertTrue("The key does not exists " + key,
                        dispatcher.routingMap.containsKey(key));
                assertTrue("Invalid number of task tables for " + key,
                        route.getRouteTo().getTaskTables()
                                .size() == dispatcher.routingMap.get(key)
                                        .size());
            });

        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    /**
     * Test the initialization failed with right exception when the XML
     * converision raises a JAXBException
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws AbstractCodedException
     */
    @Test
    public void testInitializeKoJAXBException()
            throws IOException, JAXBException, AbstractCodedException {
        // Mock the XML converter to send error
        doThrow(new JAXBException("raise exception")).when(xmlConverter)
                .convertFromXMLToObject(Mockito.anyString());

        // Set the expected exception
        thrown.expect(InternalErrorException.class);
        thrown.expectMessage("annot parse routing");
        thrown.expectCause(isA(JAXBException.class));

        this.dispatcher.initialize();
    }

    /**
     * Test the initialization failed with right exception when the XML
     * converision raises a IOException
     * 
     * @throws IOException
     * @throws JAXBException
     * @throws AbstractCodedException
     */
    @Test
    public void testInitializeKoIOException()
            throws IOException, JAXBException, AbstractCodedException {
        // Mock the XML converter to send error
        doThrow(new IOException("raise exception")).when(xmlConverter)
                .convertFromXMLToObject(Mockito.anyString());

        // Set the expected exception
        thrown.expect(InternalErrorException.class);
        thrown.expectMessage("annot parse routing");
        thrown.expectCause(isA(IOException.class));

        this.dispatcher.initialize();
    }

    private AppDataJobDto<LevelProductDto> buildAppDataJobDto(
            String satelliteId, String acquisition)
            throws InternalErrorException {
        AppDataJobDto<LevelProductDto> appDataJob =
                TestL1Utils.buildJobGeneration(false);
        appDataJob.getProduct().setAcquisition(acquisition);
        appDataJob.getProduct().setSatelliteId(satelliteId);
        return appDataJob;
    }

    @Test
    public void testGetTaskTablesIWA() throws ParseException {
        try {
            AppDataJobDto<LevelProductDto> jobA = buildAppDataJobDto("A", "IW");
            this.dispatcher.initialize();
            assertEquals(5, this.dispatcher.getTaskTables(jobA).size());

        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    @Test
    public void testDispatchIWB() throws ParseException {
        try {
            AppDataJobDto<LevelProductDto> jobA = buildAppDataJobDto("B", "IW");
            this.dispatcher.initialize();
            assertEquals(3, this.dispatcher.getTaskTables(jobA).size());

        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    @Test
    public void testDispatchOther() throws ParseException {
        try {
            AppDataJobDto<LevelProductDto> jobA = buildAppDataJobDto("A", "EW");
            this.dispatcher.initialize();
            assertEquals(5, this.dispatcher.getTaskTables(jobA).size());

        } catch (AbstractCodedException e) {
            fail("Invalid raised exception: " + e.getMessage());
        }
    }

    @Test(expected = AbstractCodedException.class)
    public void testDispatchInvalid()
            throws ParseException, AbstractCodedException {
        AppDataJobDto<LevelProductDto> jobA = buildAppDataJobDto("A", "ZZ");
        this.dispatcher.initialize();
        this.dispatcher.getTaskTables(jobA).size();
    }
}
