package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobSearchApiError;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerBuildTaskTableException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AppConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.InputWaitingConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.WaitTempo;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.XmlConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGeneration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProc;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutCheckerImpl;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class AbstractJobsGeneratorTest {

	static final List<SearchMetadata> IW_RAW__0S = Arrays.asList(new SearchMetadata(
    		"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
    		"IW_RAW__0S",
    		"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
    		"2017-12-13T12:16:23.00000Z", "2017-12-13T12:16:56.00000Z",
    		"S1",
    		"A",
    		"WILE"));
    
	static final List<SearchMetadata> IW_RAW__0A = Arrays.asList(new SearchMetadata(
    		"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
    		"IW_RAW__0A",
    		"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
    		"2017-12-13T12:11:23.00000Z", "2017-12-13T12:19:47.00000Z",
    		"S1",
    		"A",
    		"WILE"));
    
	static final List<SearchMetadata> IW_RAW__0C = Arrays.asList(new SearchMetadata(
    		"S1A_IW_RAW__0CDV_20171213T121123_20171213T121947_019684_021735_E131.SAFE",
    		"IW_RAW__0C",
    		"S1A_IW_RAW__0CDV_20171213T121123_20171213T121947_019684_021735_E131.SAFE",
    		"2017-12-13T12:11:23.00000Z", "2017-12-13T12:19:47.00000Z",
    		"S1",
    		"A",
    		"WILE"));
    
	static final List<SearchMetadata> IW_RAW__0N = Arrays.asList(new SearchMetadata(
    		"S1A_IW_RAW__0NDV_20171213T121123_20171213T121947_019684_021735_87D4.SAFE",
    		"IW_RAW__0N",
    		"S1A_IW_RAW__0NDV_20171213T121123_20171213T121947_019684_021735_87D4.SAFE",
    		"2017-12-13T12:11:23.00000Z", "2017-12-13T12:19:47.00000Z",
    		"S1",
    		"A",
    		"WILE"));

	static final List<SearchMetadata> AUX_CAL = Arrays.asList(new SearchMetadata(
    		"S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE",
    		"AUX_CAL",
    		"S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE",
    		"2017-10-17T08:00:00.00000Z", "9999-12-31T23:59:59.00000Z",
    		"S1",
    		"A",
    		"WILE"));
    
	static final List<SearchMetadata> AUX_INS = Arrays.asList(new SearchMetadata(
    		"S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE",
    		"AUX_INS",
    		"S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE",
    		"2017-10-17T08:00:00.00000Z", "9999-12-31T23:59:59.00000Z",
    		"S1",
    		"A",
    		"WILE"));

	static final List<SearchMetadata> AUX_PP1 = Arrays.asList(new SearchMetadata(
    		"S1A_AUX_PP1_V20171017T080000_G20171013T101236.SAFE",
    		"AUX_PP1",
    		"S1A_AUX_PP1_V20171017T080000_G20171013T101236.SAFE",
    		"2017-10-17T08:00:00.00000Z", "9999-12-31T23:59:59.00000Z",
    		"S1",
    		"A",
    		"WILE"));

	static final List<SearchMetadata> AUX_RES = Arrays.asList(new SearchMetadata(
    		"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
    		"AUX_OBMEMC",
    		"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
    		"2017-12-13T10:27:37.00000Z", "2017-12-13T13:45:07.00000Z",
    		"S1",
    		"A",
    		"WILE"));

	static final List<SearchMetadata> AUX_POE = Arrays.asList(new SearchMetadata(
    		"S1A_OPER_AUX_POEORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
    		"AUX_OBMEMC",
    		"S1A_OPER_AUX_POEORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
    		"2017-12-13T10:27:37.00000Z", "2017-12-13T13:45:07.00000Z",
    		"S1",
    		"A",
    		"WILE"));
	
	static final List<SearchMetadata> AUX_ATT = Arrays.asList(new SearchMetadata(
    		"S1A_OPER_AUX_ATTORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
    		"AUX_OBMEMC",
    		"S1A_OPER_AUX_ATTORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
    		"2017-12-13T10:27:37.00000Z", "2017-12-13T13:45:07.00000Z",
    		"S1",
    		"A",
    		"WILE"));
	
    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private XmlConverter xmlConverter;

    @Mock
    private MetadataClient metadataClient;

    @Mock
    private ProcessSettings processSettings;
    
    @Mock
    private MqiClient mqiClient;

    @Mock
    private IpfPreparationWorkerSettings ipfPreparationWorkerSettings;

    private int nbLoopMetadata;

    private AbstractJobsGenerator generator;

    @Mock
    private AppCatalogJobClient<CatalogEvent> appDataPService;

    @Mock
    private ProcessConfiguration processConfiguration;

    @Mock
    private JobGeneration jobGeneration;
    
    @Mock
    private AppDataJobProduct appDataJobProduct;
    
    @Mock
    private AppDataJob<CatalogEvent> appDataJob;
    
    @Mock
    private JobOrder jobOrder;
    
	private Map<String, List<SearchMetadata>> metadataBrain;
    
    private TaskTable expectedTaskTable;

    private TaskTableFactory ttFactory = new TaskTableFactory(new XmlConfig().xmlConverter());

    /**
     * Test set up
     * 
     * @throws Exception
     */
    @Before
    public void init() throws Exception {
    	
    	metadataBrain = new HashMap<>();
    	metadataBrain.put("IW_RAW__0S", IW_RAW__0S);
    	metadataBrain.put("IW_RAW__0A", IW_RAW__0A);
    	metadataBrain.put("IW_RAW__0C", IW_RAW__0C);
    	metadataBrain.put("IW_RAW__0N", IW_RAW__0N);
    	metadataBrain.put("AUX_CAL", AUX_CAL);
    	metadataBrain.put("AUX_INS", AUX_INS);
    	metadataBrain.put("AUX_PP1", AUX_PP1);
    	metadataBrain.put("AUX_RES", AUX_RES);
    	metadataBrain.put("AUX_POE", AUX_POE);
    	metadataBrain.put("AUX_ATT", AUX_ATT);
    	
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

        generator = makeUut();
        generator.initialize();
    }
    
	private LevelProductsJobsGenerator makeUut() throws IpfPrepWorkerBuildTaskTableException {
		return makeUut(InputTimeoutChecker.NULL);
	}

	private LevelProductsJobsGenerator makeUut(final InputTimeoutChecker inputTimeoutChecker)
			throws IpfPrepWorkerBuildTaskTableException {
		return new LevelProductsJobsGenerator(
        		xmlConverter, 
        		metadataClient,
                processSettings, 
                ipfPreparationWorkerSettings, 
                appDataPService, 
                processConfiguration,
                mqiClient,
                inputTimeoutChecker,
                "IW_RAW__0_GRDH_1.xml",
                ttFactory.buildTaskTable(
                		new File("./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml"), 
                		ApplicationLevel.L1
                ),
                ProductMode.SLICING            
        );
	}

    private void mockProcessSettings() {
        Mockito.doAnswer(i -> {
            final Map<String, String> r = new HashMap<String, String>(2);
            return r;
        }).when(processSettings).getParams();
        Mockito.doAnswer(i -> {
            final Map<String, String> r = new HashMap<String, String>(5);
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
        }).when(processSettings).getTriggerHostname();
    }

    private void mockJobGeneratorSettings() {
        Mockito.doAnswer(i -> {
            final Map<String, ProductFamily> r = new HashMap<>();
            r.put("IW_GRDH_1S", ProductFamily.L1_SLICE);
            r.put("IW_GRDH_1A", ProductFamily.L1_ACN);
            r.put("IW_RAW__0S", ProductFamily.L0_SLICE);
            r.put("IW_RAW__0A", ProductFamily.L0_ACN);
            r.put("IW_RAW__0C", ProductFamily.L0_ACN);
            r.put("IW_RAW__0N", ProductFamily.L0_ACN);
            return r;
        }).when(ipfPreparationWorkerSettings).getOutputfamilies();
        
        Mockito.doAnswer(i -> {
            final Map<String, ProductFamily> r = new HashMap<>();
            r.put("IW_GRDH_1S", ProductFamily.L1_SLICE);
            r.put("IW_GRDH_1A", ProductFamily.L1_ACN);
            r.put("IW_RAW__0S", ProductFamily.L0_SLICE);
            r.put("IW_RAW__0A", ProductFamily.L0_ACN);
            r.put("IW_RAW__0C", ProductFamily.L0_ACN);
            r.put("IW_RAW__0N", ProductFamily.L0_ACN);
            return r;
        }).when(ipfPreparationWorkerSettings).getInputfamilies();
        
        Mockito.doAnswer(i -> {
            return ProductFamily.AUXILIARY_FILE.name();
        }).when(ipfPreparationWorkerSettings).getDefaultfamily();
        
        Mockito.doAnswer(i -> {
            return 2;
        }).when(ipfPreparationWorkerSettings).getMaxnumberofjobs();
        
        Mockito.doAnswer(i -> {
            return new WaitTempo(2000, 3);
        }).when(ipfPreparationWorkerSettings).getWaitprimarycheck();
        
        Mockito.doAnswer(i -> {
            return new WaitTempo(10000, 3);
        }).when(ipfPreparationWorkerSettings).getWaitmetadatainput();
        
        final InputWaitingConfig inputWaitingConfig = new InputWaitingConfig();
        inputWaitingConfig.setProcessorNameRegexp(".*");
        inputWaitingConfig.setProcessorVersionRegexp(".*");
        inputWaitingConfig.setInputIdRegexp("Orbit");
        inputWaitingConfig.setTimelinessRegexp(".*");
        inputWaitingConfig.setWaitingInSeconds(600);
        inputWaitingConfig.setDelayInSeconds(0L);
        Mockito.when(ipfPreparationWorkerSettings.getInputWaiting()).thenReturn(Arrays.asList(inputWaitingConfig));
    }

    private void mockXmlConverter(final TaskTable table)
            throws IOException, JAXBException {
        Mockito.when(xmlConverter.convertFromXMLToObject(Mockito.anyString()))
                .thenReturn(table);
        Mockito.doAnswer(i -> {
            final AnnotationConfigApplicationContext ctx =
                    new AnnotationConfigApplicationContext();
            ctx.register(AppConfig.class);
            ctx.refresh();
            final XmlConverter xmlConverter = ctx.getBean(XmlConverter.class);
            final String r =
                    xmlConverter.convertFromObjectToXMLString(i.getArgument(0));
            ctx.close();
            return r;
        }).when(xmlConverter).convertFromObjectToXMLString(Mockito.any());
    }

    private void mockMetadataService(final int maxLoop1, final int maxLoop2) {
        try {
            Mockito.doAnswer(i -> {
                return null;
            }).when(this.metadataClient).getEdrsSession(Mockito.anyString(),
                    Mockito.anyString());
            Mockito.doAnswer(i -> {
                if (this.nbLoopMetadata >= maxLoop2) {
                    this.nbLoopMetadata = 0;
                    final SearchMetadataQuery query = i.getArgument(0);
                    final List<SearchMetadata> result = metadataBrain.get(query.getProductType().toUpperCase());
                    return result != null ? result : Collections.emptyList();
                }
                return null;
            }).when(this.metadataClient).search(Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.anyString(), Mockito.anyInt(),
                    Mockito.anyString(), Mockito.any());
        } catch (final MetadataQueryException e) {
            fail(e.getMessage());
        }
    }

    private void mockKafkaSender() throws AbstractCodedException {
        Mockito.doAnswer(i -> {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("./tmp/jobDtoGeneric.json"),
                    i.getArgument(0));
            return null;
        }).when(this.mqiClient).publish(Mockito.any(), Mockito.any());
    }

    private void mockAppDataService()
            throws InternalErrorException, AbstractCodedException {

        doReturn(Arrays.asList(TestL1Utils.buildJobGeneration(false)))
                .when(appDataPService)
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.anyString(), Mockito.anyString());
        final AppDataJob<CatalogEvent> primaryCheckAppJob = TestL1Utils.buildJobGeneration(true);
        primaryCheckAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationState.PRIMARY_CHECK);
        final AppDataJob<CatalogEvent> readyAppJob = TestL1Utils.buildJobGeneration(true);
        readyAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationState.READY);
        final AppDataJob<CatalogEvent> sentAppJob = TestL1Utils.buildJobGeneration(true);
        sentAppJob.getGenerations().get(0)
                .setState(AppDataJobGenerationState.SENT);
        doReturn(TestL1Utils.buildJobGeneration(true)).when(appDataPService)
                .patchJob(Mockito.eq(123L), Mockito.any(), Mockito.anyBoolean(),
                        Mockito.anyBoolean(), Mockito.anyBoolean());
        doReturn(primaryCheckAppJob).when(appDataPService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationState.PRIMARY_CHECK));
        doReturn(readyAppJob).when(appDataPService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationState.READY));
        doReturn(sentAppJob).when(appDataPService).patchTaskTableOfJob(
                Mockito.eq(123L), Mockito.eq("IW_RAW__0_GRDH_1.xml"),
                Mockito.eq(AppDataJobGenerationState.SENT));
    }

    // ---------------------------------------------------------
    // INITIALIZATION
    // @see JobsGeneratorFactoryTest
    // ---------------------------------------------------------
    @Test
    public void testInitializeWhenTaskTableIOException()
            throws IOException, JAXBException, IpfPrepWorkerBuildTaskTableException {
        doThrow(new IOException("IO exception raised")).when(xmlConverter)
                .convertFromXMLToObject(Mockito.anyString());
        
        ttFactory = new TaskTableFactory(xmlConverter);
        thrown.expect(IpfPrepWorkerBuildTaskTableException.class);
        thrown.expect(hasProperty("taskTable", is("IW_RAW__0_GRDH_1.xml")));
        thrown.expectMessage("IO exception raised");
        thrown.expectCause(isA(IOException.class));
        final AbstractJobsGenerator gen = makeUut();
        gen.initialize();
    }

    @Test
    public void testInitializeWhenTaskTableJAXBException()
            throws IOException, JAXBException, IpfPrepWorkerBuildTaskTableException {
        doThrow(new JAXBException("JAXB exception raised")).when(xmlConverter)
                .convertFromXMLToObject(Mockito.anyString());
        
        ttFactory = new TaskTableFactory(xmlConverter);        
        thrown.expect(IpfPrepWorkerBuildTaskTableException.class);
        thrown.expect(hasProperty("taskTable", is("IW_RAW__0_GRDH_1.xml")));
        thrown.expectMessage("JAXB exception raised");
        thrown.expectCause(isA(JAXBException.class));
        final AbstractJobsGenerator gen = makeUut();
        gen.initialize();
    }

    // ---------------------------------------------------------
    // CACHED RUN
    // @see LevelProductsJobsGeneratorTest#testRun
    // ---------------------------------------------------------

    @Test
    public void testRunWhenCannotRetrieveCurrentJobs()
            throws AbstractCodedException {
        doThrow(new AppCatalogJobSearchApiError("uri", "msg"))
                .when(appDataPService)
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.anyString(), Mockito.anyString());

        generator.run();
        verify(appDataPService, times(1))
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.eq("hostname"),
                        Mockito.eq("IW_RAW__0_GRDH_1.xml"));
        verifyNoMoreInteractions(appDataPService, mqiClient, metadataClient);
        verify(ipfPreparationWorkerSettings, never()).getWaitprimarycheck();
        verify(ipfPreparationWorkerSettings, never()).getWaitmetadatainput();
    }

    @Test
    public void testRunWhenNoJobs() throws AbstractCodedException {
        doReturn(null).when(appDataPService)
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.anyString(), Mockito.anyString());

        generator.run();
        verify(appDataPService, times(1))
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.eq("hostname"),
                        Mockito.eq("IW_RAW__0_GRDH_1.xml"));
        verifyNoMoreInteractions(appDataPService, mqiClient, metadataClient);
        verify(ipfPreparationWorkerSettings, never()).getWaitprimarycheck();
        verify(ipfPreparationWorkerSettings, never()).getWaitmetadatainput();
    }

    @Test
    public void testRunWhenEmptyJobs() throws AbstractCodedException {
        doReturn(new ArrayList<>()).when(appDataPService)
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.anyString(), Mockito.anyString());

        generator.run();
        verify(appDataPService, times(1))
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.eq("hostname"),
                        Mockito.eq("IW_RAW__0_GRDH_1.xml"));
        verifyNoMoreInteractions(appDataPService, mqiClient, metadataClient);
        verify(ipfPreparationWorkerSettings, never()).getWaitprimarycheck();
        verify(ipfPreparationWorkerSettings, never()).getWaitmetadatainput();
    }

    @Test
    public void testRunWhenInitialForNotEnoughTime() throws AbstractCodedException {
        Mockito.doAnswer(i -> {
            return new WaitTempo(10000, 3);
        }).when(ipfPreparationWorkerSettings).getWaitprimarycheck();
        Mockito.doAnswer(i -> {
            return new WaitTempo(10000, 3);
        }).when(ipfPreparationWorkerSettings).getWaitmetadatainput();
        
        final AppDataJob<CatalogEvent> job1 = new AppDataJob();
        job1.setId(12L);
        job1.getGenerations().add(new AppDataJobGeneration());
        job1.getGenerations().get(0).setTaskTable("IW_RAW__0_GRDH_1.xml");
        job1.getGenerations().get(0).setState(AppDataJobGenerationState.INITIAL);
        job1.getGenerations().get(0).setLastUpdateDate(new Date());

        doReturn(Arrays.asList(job1)).when(appDataPService)
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.anyString(), Mockito.anyString());

        generator.run();
        verify(appDataPService, times(1))
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.eq("hostname"),
                        Mockito.eq("IW_RAW__0_GRDH_1.xml"));
        verify(ipfPreparationWorkerSettings, times(1)).getWaitprimarycheck();
        verify(ipfPreparationWorkerSettings, never()).getWaitmetadatainput();
        verifyNoMoreInteractions(appDataPService, mqiClient, metadataClient);
    }

    @Test
    public void testRunWhenTransitoryStateForNotEnoughTime() throws AbstractCodedException {
        Mockito.doAnswer(i -> {
            return new WaitTempo(10000, 3);
        }).when(ipfPreparationWorkerSettings).getWaitprimarycheck();
        Mockito.doAnswer(i -> {
            return new WaitTempo(10000, 3);
        }).when(ipfPreparationWorkerSettings).getWaitmetadatainput();
        
        final AppDataJob<CatalogEvent> job1 = new AppDataJob();
        job1.setId(12L);
        job1.getGenerations().add(new AppDataJobGeneration());
        job1.getGenerations().get(0).setTaskTable("IW_RAW__0_GRDH_1.xml");
        job1.getGenerations().get(0).setState(AppDataJobGenerationState.INITIAL);
        job1.getGenerations().get(0).setLastUpdateDate(new Date());
        
        final AppDataJob<CatalogEvent> job2 = new AppDataJob();
        job2.setId(12L);
        job2.getGenerations().add(new AppDataJobGeneration());
        job2.getGenerations().get(0).setTaskTable("IW_RAW__0_GRDH_1.xml");
        job2.getGenerations().get(0).setState(AppDataJobGenerationState.PRIMARY_CHECK);
        job2.getGenerations().get(0).setLastUpdateDate(new Date());

        doReturn(Arrays.asList(job1, job2)).when(appDataPService)
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.anyString(), Mockito.anyString());

        generator.run();
        verify(appDataPService, times(1))
                .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                        Mockito.eq("hostname"),
                        Mockito.eq("IW_RAW__0_GRDH_1.xml"));
        verify(ipfPreparationWorkerSettings, times(1)).getWaitprimarycheck();
        verify(ipfPreparationWorkerSettings, times(1)).getWaitmetadatainput();
        verifyNoMoreInteractions(appDataPService, mqiClient, metadataClient);
    }
    
    @Test
    public void testUseOptionalInputAlternativeOfOrder1WhenItExistsExclusively() throws IpfPrepWorkerInputsMissingException {
    	final Map<Integer, SearchMetadataResult> metadataQueries = new HashMap<>();
    	final List<JobOrderProc> jobOrderProcList = new ArrayList<>();
    	Stream.of(
    		new SearchMetadataQuery(1, "LatestValCover", 0.0, 0.0, "AUX_INS", ProductFamily.BLANK), //
    		new SearchMetadataQuery(2, "LatestValCover", 0.0, 0.0, "AUX_CAL", ProductFamily.BLANK), //
    		new SearchMetadataQuery(3, "LatestValCover", 0.0, 0.0, "AUX_POE", ProductFamily.BLANK), //
    		new SearchMetadataQuery(4, "LatestValCover", 0.0, 0.0, "IW_RAW__0N", ProductFamily.L0_ACN), //
    		new SearchMetadataQuery(5, "LatestValCover", 0.0, 0.0, "AUX_PP1", ProductFamily.BLANK), //
    		new SearchMetadataQuery(6, "LatestValCover", 0.0, 0.0, "IW_RAW__0C", ProductFamily.L0_ACN), //
    		new SearchMetadataQuery(7, "LatestValCover", 0.0, 0.0, "IW_RAW__0S", ProductFamily.L0_SLICE), //
    		new SearchMetadataQuery(8, "LatestValCover", 0.0, 0.0, "AUX_ATT", ProductFamily.BLANK), //
    		new SearchMetadataQuery(9, "LatestValCover", 0.0, 0.0, "AUX_RES", ProductFamily.BLANK), //
    		new SearchMetadataQuery(10, "LatestValCover", 0.0, 0.0, "IW_RAW__0A", ProductFamily.L0_ACN) //
    	).forEach(s -> {
    		metadataQueries.put(s.getIdentifier(), new SearchMetadataResult(s));
    		jobOrderProcList.add(new JobOrderProc());
    	});

    	Mockito.when(jobGeneration.getAppDataJob()).thenReturn(appDataJob);
    	Mockito.when(jobGeneration.getMetadataQueries()).thenReturn(metadataQueries);
    	Mockito.when(appDataJob.getId()).thenReturn(23L);
    	Mockito.when(appDataJob.getProduct()).thenReturn(appDataJobProduct);
    	Mockito.when(appDataJob.getMessages()).thenReturn(Arrays.asList(
    		new GenericMessageDto<CatalogEvent>() {{
    			setBody(new CatalogEvent() {{
    				setMetadata(Collections.singletonMap("timeliness", "NRT"));
    			}});
    		}}
    	));
    	Mockito.when(appDataJobProduct.getStartTime()).thenReturn("2000-01-01T00:00:00.000000Z");
    	Mockito.when(appDataJobProduct.getStopTime()).thenReturn("2000-01-01T00:00:00.000000Z");
    	Mockito.when(appDataJobProduct.getMissionId()).thenReturn("S1");
    	Mockito.when(appDataJobProduct.getSatelliteId()).thenReturn("A");
    	Mockito.when(appDataJobProduct.getInsConfId()).thenReturn(0);
    	Mockito.when(appDataJobProduct.getProcessMode()).thenReturn("NRT");    	
    	Mockito.when(jobGeneration.getJobOrder()).thenReturn(jobOrder);    	
    	Mockito.when(jobOrder.getProcs()).thenReturn(jobOrderProcList);
    	
    	metadataBrain.remove("AUX_RES"); // removal of AUX_RES (order 2) will leave only AUX_POE (order 1) in DB
    	
    	// preconditions
    	assertNotNull(metadataBrain.get("AUX_POE"));
    	assertNull(metadataBrain.get("AUX_RES"));
    	
    	generator.inputsSearch(jobGeneration);
    	
    	// postconditions
    	final List<String> inputs = jobOrderProcList.stream().flatMap(r -> r.getInputs().stream())
    			.map(JobOrderInput::getFileType).distinct().collect(Collectors.toList());
    	assertTrue(inputs.contains("AUX_POE"));
    	assertFalse(inputs.contains("AUX_RES"));
    }
    
    @Test
    public void testUseOptionalInputAlternativeOfOrder2WhenItExistsExclusively() throws IpfPrepWorkerInputsMissingException {
    	final Map<Integer, SearchMetadataResult> metadataQueries = new HashMap<>();
    	final List<JobOrderProc> jobOrderProcList = new ArrayList<>();
    	Stream.of(
    		new SearchMetadataQuery(1, "LatestValCover", 0.0, 0.0, "AUX_INS", ProductFamily.BLANK), //
    		new SearchMetadataQuery(2, "LatestValCover", 0.0, 0.0, "AUX_CAL", ProductFamily.BLANK), //
    		new SearchMetadataQuery(3, "LatestValCover", 0.0, 0.0, "AUX_POE", ProductFamily.BLANK), //
    		new SearchMetadataQuery(4, "LatestValCover", 0.0, 0.0, "IW_RAW__0N", ProductFamily.L0_ACN), //
    		new SearchMetadataQuery(5, "LatestValCover", 0.0, 0.0, "AUX_PP1", ProductFamily.BLANK), //
    		new SearchMetadataQuery(6, "LatestValCover", 0.0, 0.0, "IW_RAW__0C", ProductFamily.L0_ACN), //
    		new SearchMetadataQuery(7, "LatestValCover", 0.0, 0.0, "IW_RAW__0S", ProductFamily.L0_SLICE), //
    		new SearchMetadataQuery(8, "LatestValCover", 0.0, 0.0, "AUX_ATT", ProductFamily.BLANK), //
    		new SearchMetadataQuery(9, "LatestValCover", 0.0, 0.0, "AUX_RES", ProductFamily.BLANK), //
    		new SearchMetadataQuery(10, "LatestValCover", 0.0, 0.0, "IW_RAW__0A", ProductFamily.L0_ACN) //
    	).forEach(s -> {
    		metadataQueries.put(s.getIdentifier(), new SearchMetadataResult(s));
    		jobOrderProcList.add(new JobOrderProc());
    	});

    	Mockito.when(jobGeneration.getAppDataJob()).thenReturn(appDataJob);
    	Mockito.when(jobGeneration.getMetadataQueries()).thenReturn(metadataQueries);
    	Mockito.when(appDataJob.getId()).thenReturn(23L);
    	Mockito.when(appDataJob.getProduct()).thenReturn(appDataJobProduct);
    	Mockito.when(appDataJob.getMessages()).thenReturn(Arrays.asList(
    		new GenericMessageDto<CatalogEvent>() {{
    			setBody(new CatalogEvent() {{
    				setMetadata(Collections.singletonMap("timeliness", "NRT"));
    			}});
    		}}
    	));
    	Mockito.when(appDataJobProduct.getStartTime()).thenReturn("2000-01-01T00:00:00.000000Z");
    	Mockito.when(appDataJobProduct.getStopTime()).thenReturn("2000-01-01T00:00:00.000000Z");
    	Mockito.when(appDataJobProduct.getMissionId()).thenReturn("S1");
    	Mockito.when(appDataJobProduct.getSatelliteId()).thenReturn("A");
    	Mockito.when(appDataJobProduct.getInsConfId()).thenReturn(0);
    	Mockito.when(appDataJobProduct.getProcessMode()).thenReturn("NRT");    	
    	Mockito.when(jobGeneration.getJobOrder()).thenReturn(jobOrder);    	
    	Mockito.when(jobOrder.getProcs()).thenReturn(jobOrderProcList);

    	metadataBrain.remove("AUX_POE"); // removal of AUX_POE (order 1) will leave only AUX_RES (order 2) in DB

    	// preconditions
    	assertNull(metadataBrain.get("AUX_POE"));
    	assertNotNull(metadataBrain.get("AUX_RES"));
    	
    	generator.inputsSearch(jobGeneration);
    	
    	// postconditions
    	final List<String> inputs = jobOrderProcList.stream().flatMap(r -> r.getInputs().stream())
    			.map(JobOrderInput::getFileType).distinct().collect(Collectors.toList());
    	assertFalse(inputs.contains("AUX_POE"));
    	assertTrue(inputs.contains("AUX_RES"));
    }
    
    @Test
    public void testUseOptionalInputAlternativeOfOrder1WhenMultipleOrdersExist() throws IpfPrepWorkerInputsMissingException {
    	final Map<Integer, SearchMetadataResult> metadataQueries = new HashMap<>();
    	final List<JobOrderProc> jobOrderProcList = new ArrayList<>();
    	Stream.of(
    		new SearchMetadataQuery(1, "LatestValCover", 0.0, 0.0, "AUX_INS", ProductFamily.BLANK), //
    		new SearchMetadataQuery(2, "LatestValCover", 0.0, 0.0, "AUX_CAL", ProductFamily.BLANK), //
    		new SearchMetadataQuery(3, "LatestValCover", 0.0, 0.0, "AUX_POE", ProductFamily.BLANK), //
    		new SearchMetadataQuery(4, "LatestValCover", 0.0, 0.0, "IW_RAW__0N", ProductFamily.L0_ACN), //
    		new SearchMetadataQuery(5, "LatestValCover", 0.0, 0.0, "AUX_PP1", ProductFamily.BLANK), //
    		new SearchMetadataQuery(6, "LatestValCover", 0.0, 0.0, "IW_RAW__0C", ProductFamily.L0_ACN), //
    		new SearchMetadataQuery(7, "LatestValCover", 0.0, 0.0, "IW_RAW__0S", ProductFamily.L0_SLICE), //
    		new SearchMetadataQuery(8, "LatestValCover", 0.0, 0.0, "AUX_ATT", ProductFamily.BLANK), //
    		new SearchMetadataQuery(9, "LatestValCover", 0.0, 0.0, "AUX_RES", ProductFamily.BLANK), //
    		new SearchMetadataQuery(10, "LatestValCover", 0.0, 0.0, "IW_RAW__0A", ProductFamily.L0_ACN) //
    	).forEach(s -> {
    		metadataQueries.put(s.getIdentifier(), new SearchMetadataResult(s));
    		jobOrderProcList.add(new JobOrderProc());
    	});

    	Mockito.when(jobGeneration.getAppDataJob()).thenReturn(appDataJob);
    	Mockito.when(jobGeneration.getMetadataQueries()).thenReturn(metadataQueries);
    	Mockito.when(appDataJob.getId()).thenReturn(23L);
    	Mockito.when(appDataJob.getProduct()).thenReturn(appDataJobProduct);
    	Mockito.when(appDataJob.getMessages()).thenReturn(Arrays.asList(
    		new GenericMessageDto<CatalogEvent>() {{
    			setBody(new CatalogEvent() {{
    				setMetadata(Collections.singletonMap("timeliness", "NRT"));
    			}});
    		}}
    	));
    	Mockito.when(appDataJobProduct.getStartTime()).thenReturn("2000-01-01T00:00:00.000000Z");
    	Mockito.when(appDataJobProduct.getStopTime()).thenReturn("2000-01-01T00:00:00.000000Z");
    	Mockito.when(appDataJobProduct.getMissionId()).thenReturn("S1");
    	Mockito.when(appDataJobProduct.getSatelliteId()).thenReturn("A");
    	Mockito.when(appDataJobProduct.getInsConfId()).thenReturn(0);
    	Mockito.when(appDataJobProduct.getProcessMode()).thenReturn("NRT");    	
    	Mockito.when(jobGeneration.getJobOrder()).thenReturn(jobOrder);    	
    	Mockito.when(jobOrder.getProcs()).thenReturn(jobOrderProcList);

    	// preconditions
    	assertNotNull(metadataBrain.get("AUX_POE"));
    	assertNotNull(metadataBrain.get("AUX_RES"));

    	generator.inputsSearch(jobGeneration);
    	
    	// postconditions
    	final List<String> inputs = jobOrderProcList.stream().flatMap(r -> r.getInputs().stream())
    			.map(JobOrderInput::getFileType).distinct().collect(Collectors.toList());
    	assertTrue(inputs.contains("AUX_POE"));
    	assertFalse(inputs.contains("AUX_RES"));
    }

    @Test
    @Ignore
    public void testWaiting() throws Exception {
    	final Map<Integer, SearchMetadataResult> metadataQueries = new HashMap<>();
    	final List<JobOrderProc> jobOrderProcList = new ArrayList<>();
    	Stream.of(
    		new SearchMetadataQuery(1, "LatestValCover", 0.0, 0.0, "AUX_INS", ProductFamily.BLANK), //
    		new SearchMetadataQuery(2, "LatestValCover", 0.0, 0.0, "AUX_CAL", ProductFamily.BLANK), //
    		new SearchMetadataQuery(3, "LatestValCover", 0.0, 0.0, "AUX_POE", ProductFamily.BLANK), //
    		new SearchMetadataQuery(4, "LatestValCover", 0.0, 0.0, "IW_RAW__0N", ProductFamily.L0_ACN), //
    		new SearchMetadataQuery(5, "LatestValCover", 0.0, 0.0, "AUX_PP1", ProductFamily.BLANK), //
    		new SearchMetadataQuery(6, "LatestValCover", 0.0, 0.0, "IW_RAW__0C", ProductFamily.L0_ACN), //
    		new SearchMetadataQuery(7, "LatestValCover", 0.0, 0.0, "IW_RAW__0S", ProductFamily.L0_SLICE), //
    		new SearchMetadataQuery(8, "LatestValCover", 0.0, 0.0, "AUX_ATT", ProductFamily.BLANK), //
    		new SearchMetadataQuery(9, "LatestValCover", 0.0, 0.0, "AUX_RES", ProductFamily.BLANK), //
    		new SearchMetadataQuery(10, "LatestValCover", 0.0, 0.0, "IW_RAW__0A", ProductFamily.L0_ACN) //
    	).forEach(s -> {
    		metadataQueries.put(s.getIdentifier(), new SearchMetadataResult(s));
    		jobOrderProcList.add(new JobOrderProc());
    	});

    	Mockito.when(jobGeneration.getAppDataJob()).thenReturn(appDataJob);
    	Mockito.when(jobGeneration.getMetadataQueries()).thenReturn(metadataQueries);
    	Mockito.when(appDataJob.getId()).thenReturn(23L);
    	Mockito.when(appDataJob.getProduct()).thenReturn(appDataJobProduct);
    	Mockito.when(appDataJob.getMessages()).thenReturn(Arrays.asList(
    		new GenericMessageDto<CatalogEvent>() {{
    			setBody(new CatalogEvent() {{
    				setMetadata(Collections.singletonMap("timeliness", "NRT"));
    			}});
    		}}
    	));
    	
    	Mockito.when(appDataJobProduct.getStartTime()).thenReturn("2000-01-01T00:00:00.000000Z");
    	Mockito.when(appDataJobProduct.getStopTime()).thenReturn("2000-01-01T00:00:00.000000Z");
    	Mockito.when(appDataJobProduct.getMissionId()).thenReturn("S1");
    	Mockito.when(appDataJobProduct.getSatelliteId()).thenReturn("A");
    	Mockito.when(appDataJobProduct.getInsConfId()).thenReturn(0);
    	Mockito.when(appDataJobProduct.getProcessMode()).thenReturn("NRT");    	
    	Mockito.when(jobGeneration.getJobOrder()).thenReturn(jobOrder);    	
    	Mockito.when(jobOrder.getProcs()).thenReturn(jobOrderProcList);
    	
    	metadataBrain.remove("AUX_POE");
    	metadataBrain.remove("AUX_RES");
    	
    	// preconditions
    	assertNull(metadataBrain.get("AUX_POE"));
    	assertNull(metadataBrain.get("AUX_RES"));
    
    	// Part 1: Exactly before timeout exceeds
    	   	
    	final InputTimeoutChecker inputTimeoutChecker1 = new InputTimeoutCheckerImpl(
    			ipfPreparationWorkerSettings.getInputWaiting(),
			new Supplier<LocalDateTime>() {
	    		@Override
				public LocalDateTime get() {
					return DateUtils.parse("2000-01-01T00:09:59.999999Z");
	    		}
	    	}
    	);
    	
    	generator = makeUut(inputTimeoutChecker1);
    	generator.initialize();
    	
    	assertThatThrownBy(() -> generator.inputsSearch(jobGeneration))
    		.isInstanceOf(IpfPrepWorkerInputsMissingException.class);

    	// Part 2: Exactly when timeout is exceeded
    	
    	final InputTimeoutChecker inputTimeoutChecker2 = new InputTimeoutCheckerImpl(
    			ipfPreparationWorkerSettings.getInputWaiting(),
			new Supplier<LocalDateTime>() {
	    		@Override
				public LocalDateTime get() {
					return DateUtils.parse("2000-01-01T00:10:00.000000Z");
	    		}
	    	}
    	);
    	
    	generator = makeUut(inputTimeoutChecker2);
    	generator.initialize();
    	
    	generator.inputsSearch(jobGeneration);
    	    	
    	// postconditions
    	final List<String> inputs = jobOrderProcList.stream().flatMap(r -> r.getInputs().stream())
    			.map(JobOrderInput::getFileType).distinct().collect(Collectors.toList());
    	assertFalse(inputs.contains("AUX_POE"));
    	assertFalse(inputs.contains("AUX_RES"));    	
    }

}