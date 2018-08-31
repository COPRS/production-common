package esa.s1pdgs.cpoc.jobgenerator.tasks.generator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.io.IOException;
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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.jobgenerator.config.AppConfig;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings.WaitTempo;
import esa.s1pdgs.cpoc.jobgenerator.model.ProductMode;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataQuery;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.generator.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.generator.LevelProductsJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestGenericUtils;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL1Utils;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

public class AbstractJobsGeneratorTest {

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private XmlConverter xmlConverter;

    @Mock
    private MetadataService metadataService;

    @Mock
    private ProcessSettings processSettings;

    @Mock
    private JobGeneratorSettings jobGeneratorSettings;

    @Mock
    private OutputProcuderFactory JobsSender;

    private int nbLoopMetadata;

    private AbstractJobsGenerator<LevelProductDto> generator;

    @Mock
    private AbstractAppCatalogJobService<LevelProductDto> appDataPService;

    private TaskTable expectedTaskTable;

    /**
     * Test set up
     * 
     * @throws Exception
     */
    @Before
    public void init() throws Exception {
        this.nbLoopMetadata = 0;
        expectedTaskTable = TestGenericUtils.buildTaskTableIW();

        // Mcokito
        MockitoAnnotations.initMocks(this);

        // Mock process settings
        this.mockProcessSettings();

        // Mock job generator settings
        this.mockJobGeneratorSettings();

        // Mock XML converter
        this.mockXmlConverter(expectedTaskTable);

        // Mock metadata service
        this.mockMetadataService(0, 0);

        this.mockKafkaSender();

        this.mockAppDataService();

        generator = new LevelProductsJobsGenerator(xmlConverter,
                metadataService, processSettings, jobGeneratorSettings,
                JobsSender, appDataPService);
        generator.initialize(new File(
                "./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml"));
        generator.setMode(ProductMode.SLICING);
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
    }

    private void mockJobGeneratorSettings() {
        Mockito.doAnswer(i -> {
            Map<String, ProductFamily> r = new HashMap<>();
            r.put("IW_GRDH_1S", ProductFamily.L1_PRODUCT);
            r.put("IW_GRDH_1A", ProductFamily.L1_ACN);
            r.put("IW_RAW__0S", ProductFamily.L0_PRODUCT);
            r.put("IW_RAW__0A", ProductFamily.L0_ACN);
            r.put("IW_RAW__0C", ProductFamily.L0_ACN);
            r.put("IW_RAW__0N", ProductFamily.L0_ACN);
            return r;
        }).when(jobGeneratorSettings).getOutputfamilies();
        Mockito.doAnswer(i -> {
            return ProductFamily.AUXILIARY_FILE.name();
        }).when(jobGeneratorSettings).getDefaultfamily();
        Mockito.doAnswer(i -> {
            return 2;
        }).when(jobGeneratorSettings).getMaxnumberofjobs();
        Mockito.doAnswer(i -> {
            return new WaitTempo(2000, 3);
        }).when(jobGeneratorSettings).getWaitprimarycheck();
        Mockito.doAnswer(i -> {
            return new WaitTempo(10000, 3);
        }).when(jobGeneratorSettings).getWaitmetadatainput();
    }

    private void mockXmlConverter(TaskTable table)
            throws IOException, JAXBException {
        Mockito.when(xmlConverter.convertFromXMLToObject(Mockito.anyString()))
                .thenReturn(table);
        Mockito.doAnswer(i -> {
            AnnotationConfigApplicationContext ctx =
                    new AnnotationConfigApplicationContext();
            ctx.register(AppConfig.class);
            ctx.refresh();
            XmlConverter xmlConverter = ctx.getBean(XmlConverter.class);
            String r =
                    xmlConverter.convertFromObjectToXMLString(i.getArgument(0));
            ctx.close();
            return r;
        }).when(xmlConverter).convertFromObjectToXMLString(Mockito.any());
    }

    private void mockMetadataService(int maxLoop1, int maxLoop2) {
        try {
            Mockito.doAnswer(i -> {
                return null;
            }).when(this.metadataService).getEdrsSession(Mockito.anyString(),
                    Mockito.anyString());
            Mockito.doAnswer(i -> {
                if (this.nbLoopMetadata >= maxLoop2) {
                    this.nbLoopMetadata = 0;
                    SearchMetadataQuery query = i.getArgument(0);
                    if ("IW_RAW__0S".equalsIgnoreCase(query.getProductType())) {
                        return new SearchMetadata(
                                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                                "IW_RAW__0S",
                                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                                "2017-12-13T12:16:23", "2017-12-13T12:16:56");
                    } else if ("IW_RAW__0A"
                            .equalsIgnoreCase(query.getProductType())) {
                        return new SearchMetadata(
                                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                                "IW_RAW__0A",
                                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                                "2017-12-13T12:11:23", "2017-12-13T12:19:47");
                    } else if ("IW_RAW__0C"
                            .equalsIgnoreCase(query.getProductType())) {
                        return new SearchMetadata(
                                "S1A_IW_RAW__0CDV_20171213T121123_20171213T121947_019684_021735_E131.SAFE",
                                "IW_RAW__0C",
                                "S1A_IW_RAW__0CDV_20171213T121123_20171213T121947_019684_021735_E131.SAFE",
                                "2017-12-13T12:11:23", "2017-12-13T12:19:47");
                    } else if ("IW_RAW__0N"
                            .equalsIgnoreCase(query.getProductType())) {
                        return new SearchMetadata(
                                "S1A_IW_RAW__0NDV_20171213T121123_20171213T121947_019684_021735_87D4.SAFE",
                                "IW_RAW__0N",
                                "S1A_IW_RAW__0NDV_20171213T121123_20171213T121947_019684_021735_87D4.SAFE",
                                "2017-12-13T12:11:23", "2017-12-13T12:19:47");
                    } else if ("AUX_CAL"
                            .equalsIgnoreCase(query.getProductType())) {
                        return new SearchMetadata(
                                "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE",
                                "AUX_CAL",
                                "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE",
                                "2017-10-17T08:00:00", "9999-12-31T23:59:59");
                    } else if ("AUX_INS"
                            .equalsIgnoreCase(query.getProductType())) {
                        return new SearchMetadata(
                                "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE",
                                "AUX_INS",
                                "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE",
                                "2017-10-17T08:00:00", "9999-12-31T23:59:59");
                    } else if ("AUX_PP1"
                            .equalsIgnoreCase(query.getProductType())) {
                        return new SearchMetadata(
                                "S1A_AUX_PP1_V20171017T080000_G20171013T101236.SAFE",
                                "AUX_PP1",
                                "S1A_AUX_PP1_V20171017T080000_G20171013T101236.SAFE",
                                "2017-10-17T08:00:00", "9999-12-31T23:59:59");
                    } else if ("AUX_RES"
                            .equalsIgnoreCase(query.getProductType())) {
                        return new SearchMetadata(
                                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
                                "AUX_OBMEMC",
                                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
                                "2017-12-13T10:27:37", "2017-12-13T13:45:07");
                    }
                }
                return null;
            }).when(this.metadataService).search(Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.anyString(), Mockito.anyInt());
        } catch (JobGenMetadataException e) {
            fail(e.getMessage());
        }
    }

    private void mockKafkaSender() throws AbstractCodedException {
        Mockito.doAnswer(i -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("./tmp/jobDtoGeneric.json"),
                    i.getArgument(0));
            return null;
        }).when(this.JobsSender).sendJob(Mockito.any(), Mockito.any());
    }

    private void mockAppDataService()
            throws InternalErrorException, AbstractCodedException {
        doReturn(Arrays.asList(TestL1Utils.buildJobGeneration(false)))
                .when(appDataPService)
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
        doReturn(TestL1Utils.buildJobGeneration(true)).when(appDataPService)
                .patchJob(Mockito.eq(123L), Mockito.any());
        doReturn(primaryCheckAppJob).when(appDataPService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationDtoState.PRIMARY_CHECK));
        doReturn(readyAppJob).when(appDataPService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationDtoState.READY));
        doReturn(sentAppJob).when(appDataPService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationDtoState.SENT));
    }

    // ---------------------------------------------------------
    // INITIALIZATION
    // @see JobsGeneratorFactoryTest
    // ---------------------------------------------------------
    @Test
    public void testInitializeWhenTaskTableIOException()
            throws IOException, JAXBException, JobGenBuildTaskTableException {
        doThrow(new IOException("IO exception raised")).when(xmlConverter)
                .convertFromXMLToObject(Mockito.anyString());
        AbstractJobsGenerator<LevelProductDto> gen =
                new LevelProductsJobsGenerator(xmlConverter, metadataService,
                        processSettings, jobGeneratorSettings, JobsSender,
                        appDataPService);
        generator.setMode(ProductMode.SLICING);

        thrown.expect(JobGenBuildTaskTableException.class);
        thrown.expect(hasProperty("taskTable", is("IW_RAW__0_GRDH_1.xml")));
        thrown.expectMessage("IO exception raised");
        thrown.expectCause(isA(IOException.class));
        gen.initialize(new File(
                "./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml"));
    }

    @Test
    public void testInitializeWhenTaskTableJAXBException()
            throws IOException, JAXBException, JobGenBuildTaskTableException {
        doThrow(new JAXBException("JAXB exception raised")).when(xmlConverter)
                .convertFromXMLToObject(Mockito.anyString());
        AbstractJobsGenerator<LevelProductDto> gen =
                new LevelProductsJobsGenerator(xmlConverter, metadataService,
                        processSettings, jobGeneratorSettings, JobsSender,
                        appDataPService);
        generator.setMode(ProductMode.SLICING);

        thrown.expect(JobGenBuildTaskTableException.class);
        thrown.expect(hasProperty("taskTable", is("IW_RAW__0_GRDH_1.xml")));
        thrown.expectMessage("JAXB exception raised");
        thrown.expectCause(isA(JAXBException.class));
        gen.initialize(new File(
                "./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml"));
    }

    // ---------------------------------------------------------
    // CACHED RUN
    // @see LevelProductsJobsGeneratorTest#testRun
    // ---------------------------------------------------------

}