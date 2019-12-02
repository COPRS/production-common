package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.levelproducts;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.L0SlicePatternSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.utils.TestL0Utils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class L1AppConsumerTest {

    @Mock
    private AbstractJobsDispatcher<CatalogEvent> l0SliceJobsDispatcher;

    @Mock
    protected ProcessSettings processSettings;

    @Mock
    private L0SlicePatternSettings l0SlicePatternSettings;

    @Mock
    private GenericMqiClient mqiService;
    @Mock
    protected StatusService mqiStatusService;

    @Mock
    protected AppCatalogJobClient appDataService;

    /**
     * Application status
     */
    @Mock
    private AppStatus appStatus;

    @Mock
    private Status jobStatus;
    
    private ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL ;

    private CatalogEvent dtoMatch = TestL0Utils.newCatalogEvent(
            "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
            "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
            ProductFamily.L0_SLICE, "NRT");
    private CatalogEvent dtoNotMatch = TestL0Utils.newCatalogEvent(
            "S1A_I_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
            "S1A_I_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
            ProductFamily.L0_SLICE, "NRT");
    private GenericMessageDto<CatalogEvent> message1 =
            new GenericMessageDto<CatalogEvent>(1, "", dtoMatch);
    private GenericMessageDto<CatalogEvent> message2 =
            new GenericMessageDto<CatalogEvent>(2, "", dtoNotMatch);
    
    @Mock
    private MetadataClient metadataClient;

    @Before
    public void setUp() throws Exception {

        // Mockito
        MockitoAnnotations.initMocks(this);

        // Mock the dispatcher
        Mockito.doAnswer(i ->  true).when(l0SliceJobsDispatcher).dispatch(Mockito.any());

        // Mock the settings
        Mockito.doReturn(
                "^([0-9a-z]{2})([0-9a-z]){1}_(([0-9a-z]{2})_RAW__0([0-9a-z_]{3}))_([0-9a-z]{15})_([0-9a-z]{15})_([0-9a-z_]{6})\\w{1,}\\.SAFE(/.*)?$")
                .when(l0SlicePatternSettings).getRegexp();
        Mockito.doReturn(2).when(l0SlicePatternSettings).getMGroupSatId();
        Mockito.doReturn(1).when(l0SlicePatternSettings).getMGroupMissionId();
        Mockito.doReturn(4).when(l0SlicePatternSettings).getMGroupAcquisition();
        Mockito.doReturn(6).when(l0SlicePatternSettings).getMGroupStartTime();
        Mockito.doReturn(7).when(l0SlicePatternSettings).getMGroupStopTime();
        Mockito.doReturn(
        		"^([0-9a-zA-Z]{2})([0-9a-zA-Z]){1}_(SM|IW|EW)_RAW__0([0-9a-zA-Z_]{3})_([0-9a-zA-Z]{15})_([0-9a-zA-Z]{15})_([0-9a-zA-Z_]{6})\\w{1,}\\.SAFE$")
                .when(l0SlicePatternSettings).getSeaCoverageCheckPattern();
        this.mockProcessSettings();

        // Mock the MQI service
        doReturn(message1, message2).when(mqiService).next(Mockito.any());
        doReturn(true).when(mqiService).ack(Mockito.any(), Mockito.any());

        // Mock app status
        doNothing().when(appStatus).setWaiting();
        doNothing().when(appStatus).setProcessing(Mockito.anyLong());
        doNothing().when(appStatus).setError(Mockito.anyString());
        doReturn(jobStatus).when(appStatus).getStatus();
        doReturn(false).when(jobStatus).isStopping();

        // Mock the appcatalog service
        doReturn(new ArrayList<>()).when(appDataService)
                .findByMessagesId(Mockito.anyLong());
        Mockito.doAnswer(i -> {
            return i.getArgument(0);
        }).when(appDataService).newJob(Mockito.any());
        Mockito.doAnswer(i -> {
            return i.getArgument(1);
        }).when(appDataService).patchJob(Mockito.anyLong(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());

        // Mock metadata service
        Mockito.doAnswer(i -> {
        	return new EdrsSessionMetadata("name", "type", "kobs", "session", "start", "stop", "vstart", "vstop", "mission", "satellite", "station",
        			Arrays.<String>asList("DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw",
        			"DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
        }).when(metadataClient).getEdrsSession(Mockito.anyString(), Mockito.anyString());
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
        }).when(processSettings).getHostname();
        Mockito.doAnswer(i -> {
            return ApplicationMode.TEST;
        }).when(processSettings).getMode();
    }

    @Test
    public void testProductNameNotMatch() throws AbstractCodedException {

        final LevelProductsConsumer consumer = new LevelProductsConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService, errorAppender, appStatus, metadataClient, 0, 0);
        consumer.onMessage(message2);

        verify(l0SliceJobsDispatcher, never()).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(2L));
        verify(appStatus, times(2)).setWaiting();
    }

    @Test
    public void testReceiveOk() throws AbstractCodedException, ParseException {
        doReturn(100).when(metadataClient).getSeaCoverage(Mockito.any(), Mockito.any()); 
        
        final LevelProductsConsumer consumer = new LevelProductsConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus, metadataClient, 0, 0);
        consumer.onMessage(message1);

        verify(appDataService, times(1)).newJob(Mockito.any());
        verify(appDataService, times(1)).patchJob(Mockito.anyLong(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());
        verify(l0SliceJobsDispatcher, times(1)).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(1L));
        verify(appStatus, times(2)).setWaiting();
    }

    @Test
    public void testReceiveNull() throws AbstractCodedException {
        doReturn(null).when(mqiService).next(Mockito.any());

        final LevelProductsConsumer consumer = new LevelProductsConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus, metadataClient, 0, 0);
        consumer.onMessage(null);

        verify(l0SliceJobsDispatcher, never()).dispatch(Mockito.any());
        verify(appStatus, never()).setProcessing(Mockito.eq(2L));
        verify(appStatus, times(1)).setWaiting();
    }

    @Test
    public void testReceiveAlreadyExistOtherPod()
            throws AbstractCodedException, ParseException {
        final AppDataJob job1 = new AppDataJob();
        job1.setId(12L);
        job1.setPod("i-hostname");
        job1.setProduct(new AppDataJobProduct());
        job1.getProduct().setProductName("p1");
        final AppDataJob job2 = new AppDataJob();
        job2.setId(24L);
        job2.setPod("other-hostname");
        job2.setProduct(new AppDataJobProduct());
        job2.getProduct().setProductName("p2");
        doReturn(Arrays.asList(job1, job2)).when(appDataService)
                .findByMessagesId(Mockito.anyLong());
        
        doReturn(100).when(metadataClient).getSeaCoverage(Mockito.any(), Mockito.any()); 

        final LevelProductsConsumer consumer = new LevelProductsConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus, metadataClient, 0, 0);
        consumer.onMessage(message1);

        job1.setPod("");
        verify(appDataService, never()).newJob(Mockito.any());
        verify(appDataService, times(2)).patchJob(Mockito.eq(12L), Mockito.any(),
                Mockito.eq(false), Mockito.eq(false),
                Mockito.eq(false));
        verify(l0SliceJobsDispatcher, times(1)).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(1L));
        verify(appStatus, times(2)).setWaiting();
    }

    @Test
    public void testReceiveAlreadyExistSamePodWaiting()
            throws AbstractCodedException, ParseException {
        final AppDataJob job1 = new AppDataJob();
        job1.setId(12L);
        job1.setPod("hostname");
        job1.setProduct(new AppDataJobProduct());
        job1.getProduct().setProductName("p1");
        final AppDataJob job2 = new AppDataJob();
        job2.setId(24L);
        job2.setPod("other-hostname");
        job2.setProduct(new AppDataJobProduct());
        job2.getProduct().setProductName("p2");
        doReturn(Arrays.asList(job1, job2)).when(appDataService)
                .findByMessagesId(Mockito.anyLong());
        
        doReturn(100).when(metadataClient).getSeaCoverage(Mockito.any(), Mockito.any());        

        final LevelProductsConsumer consumer = new LevelProductsConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus, metadataClient, 0, 0);
        consumer.onMessage(message1);

        job1.setPod("");
        verify(appDataService, never()).newJob(Mockito.any());
        verify(appDataService, times(1)).patchJob(Mockito.eq(12L), Mockito.eq(job1),
                Mockito.eq(false), Mockito.eq(false),
                Mockito.eq(false));
        verify(l0SliceJobsDispatcher, times(1)).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(1L));
        verify(appStatus, times(2)).setWaiting();
    }

    @Test
    public void testReceiveAlreadyExistSamePodGenerating()
            throws AbstractCodedException, ParseException {
        final AppDataJob job1 = new AppDataJob();
        job1.setId(12L);
        job1.setPod("hostname");
        job1.setState(AppDataJobState.DISPATCHING);
        job1.setProduct(new AppDataJobProduct());
        job1.getProduct().setProductName("p1");
        final AppDataJob job2 = new AppDataJob();
        job2.setId(24L);
        job2.setPod("other-hostname");
        job2.setProduct(new AppDataJobProduct());
        job2.getProduct().setProductName("p1");
        doReturn(Arrays.asList(job1, job2)).when(appDataService)
                .findByMessagesId(Mockito.anyLong());
        
        doReturn(100).when(metadataClient).getSeaCoverage(Mockito.any(), Mockito.any()); 

        final LevelProductsConsumer consumer = new LevelProductsConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus, metadataClient, 0, 0);
        consumer.onMessage(message1);

        job1.setPod("");
        verify(appDataService, never()).newJob(Mockito.any());
        verify(appDataService, never()).patchJob(Mockito.anyLong(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());
        verify(l0SliceJobsDispatcher, times(1)).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(1L));
        verify(appStatus, times(2)).setWaiting();
    }

}
