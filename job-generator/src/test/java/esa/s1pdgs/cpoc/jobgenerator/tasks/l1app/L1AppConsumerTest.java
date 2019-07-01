package esa.s1pdgs.cpoc.jobgenerator.tasks.l1app;

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
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.jobgenerator.config.L0SlicePatternSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus.JobStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class L1AppConsumerTest {

    @Mock
    private AbstractJobsDispatcher<ProductDto> l0SliceJobsDispatcher;

    @Mock
    protected ProcessSettings processSettings;

    @Mock
    private L0SlicePatternSettings l0SlicePatternSettings;

    @Mock
    private GenericMqiService<ProductDto> mqiService;
    @Mock
    protected StatusService mqiStatusService;

    @Mock
    protected AppCatalogJobClient<ProductDto> appDataService;

    /**
     * Application status
     */
    @Mock
    private AppStatus appStatus;

    @Mock
    private JobStatus jobStatus;
    
    private ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL ;

    private ProductDto dtoMatch = new ProductDto(
            "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
            "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
            ProductFamily.L0_SLICE, "NRT");
    private ProductDto dtoNotMatch = new ProductDto(
            "S1A_I_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
            "S1A_I_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
            ProductFamily.L0_SLICE, "NRT");
    private GenericMessageDto<ProductDto> message1 =
            new GenericMessageDto<ProductDto>(1, "", dtoMatch);
    private GenericMessageDto<ProductDto> message2 =
            new GenericMessageDto<ProductDto>(2, "", dtoNotMatch);

    @Before
    public void setUp() throws Exception {

        // Mcokito
        MockitoAnnotations.initMocks(this);

        // Mock the dispatcher
        Mockito.doAnswer(i -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
            return true;
        }).when(l0SliceJobsDispatcher).dispatch(Mockito.any());

        // Mock the settings
        Mockito.doReturn(
                "^([0-9a-z]{2})([0-9a-z]){1}_(([0-9a-z]{2})_RAW__0([0-9a-z_]{3}))_([0-9a-z]{15})_([0-9a-z]{15})_([0-9a-z_]{6})\\w{1,}\\.SAFE(/.*)?$")
                .when(l0SlicePatternSettings).getRegexp();
        Mockito.doReturn(2).when(l0SlicePatternSettings).getMGroupSatId();
        Mockito.doReturn(1).when(l0SlicePatternSettings).getMGroupMissionId();
        Mockito.doReturn(4).when(l0SlicePatternSettings).getMGroupAcquisition();
        Mockito.doReturn(6).when(l0SlicePatternSettings).getMGroupStartTime();
        Mockito.doReturn(7).when(l0SlicePatternSettings).getMGroupStopTime();
        this.mockProcessSettings();

        // Mock the MQI service
        doReturn(message1, message2).when(mqiService).next();
        doReturn(true).when(mqiService).ack(Mockito.any());

        // Mock app status
        doNothing().when(appStatus).setWaiting();
        doNothing().when(appStatus).setProcessing(Mockito.anyLong());
        doNothing().when(appStatus).setError(Mockito.anyString());
        doReturn(jobStatus).when(appStatus).getStatus();
        doReturn(false).when(jobStatus).isStopping();

        // Mock the appcatalog service
        doReturn(new ArrayList<>()).when(appDataService)
                .findByMessagesIdentifier(Mockito.anyLong());
        Mockito.doAnswer(i -> {
            return i.getArgument(0);
        }).when(appDataService).newJob(Mockito.any());
        Mockito.doAnswer(i -> {
            return i.getArgument(1);
        }).when(appDataService).patchJob(Mockito.anyLong(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());

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

    @Test
    public void testProductNameNotMatch() throws AbstractCodedException {
        doReturn(message2).when(mqiService).next();

        L1AppConsumer consumer = new L1AppConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService, errorAppender, appStatus);
        consumer.consumeMessages();

        verify(l0SliceJobsDispatcher, never()).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(2L));
        verify(appStatus, times(2)).setWaiting();
    }

    @Test
    public void testReceiveOk() throws AbstractCodedException, ParseException {
        doReturn(message1).when(mqiService).next();

        L1AppConsumer consumer = new L1AppConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus);
        consumer.consumeMessages();

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
        doReturn(null).when(mqiService).next();

        L1AppConsumer consumer = new L1AppConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus);
        consumer.consumeMessages();

        verify(l0SliceJobsDispatcher, never()).dispatch(Mockito.any());
        verify(appStatus, never()).setProcessing(Mockito.eq(2L));
        verify(appStatus, times(1)).setWaiting();
    }

    @Test
    public void testReceiveAlreadyExistOtherPod()
            throws AbstractCodedException, ParseException {
        AppDataJobDto<ProductDto> job1 = new AppDataJobDto<>();
        job1.setIdentifier(12L);
        job1.setPod("i-hostname");
        job1.setProduct(new AppDataJobProductDto());
        job1.getProduct().setProductName("p1");
        AppDataJobDto<ProductDto> job2 = new AppDataJobDto<>();
        job2.setIdentifier(24L);
        job2.setPod("other-hostname");
        job2.setProduct(new AppDataJobProductDto());
        job2.getProduct().setProductName("p2");
        doReturn(message1).when(mqiService).next();
        doReturn(Arrays.asList(job1, job2)).when(appDataService)
                .findByMessagesIdentifier(Mockito.anyLong());

        L1AppConsumer consumer = new L1AppConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus);
        consumer.consumeMessages();

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
        AppDataJobDto<ProductDto> job1 = new AppDataJobDto<>();
        job1.setIdentifier(12L);
        job1.setPod("hostname");
        job1.setProduct(new AppDataJobProductDto());
        job1.getProduct().setProductName("p1");
        AppDataJobDto<ProductDto> job2 = new AppDataJobDto<>();
        job2.setIdentifier(24L);
        job2.setPod("other-hostname");
        job2.setProduct(new AppDataJobProductDto());
        job2.getProduct().setProductName("p2");
        doReturn(message1).when(mqiService).next();
        doReturn(Arrays.asList(job1, job2)).when(appDataService)
                .findByMessagesIdentifier(Mockito.anyLong());

        L1AppConsumer consumer = new L1AppConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus);
        consumer.consumeMessages();

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
        AppDataJobDto<ProductDto> job1 = new AppDataJobDto<>();
        job1.setIdentifier(12L);
        job1.setPod("hostname");
        job1.setState(AppDataJobDtoState.DISPATCHING);
        job1.setProduct(new AppDataJobProductDto());
        job1.getProduct().setProductName("p1");
        AppDataJobDto<ProductDto> job2 = new AppDataJobDto<>();
        job2.setIdentifier(24L);
        job2.setPod("other-hostname");
        job2.setProduct(new AppDataJobProductDto());
        job2.getProduct().setProductName("p1");
        doReturn(message1).when(mqiService).next();
        doReturn(Arrays.asList(job1, job2)).when(appDataService)
                .findByMessagesIdentifier(Mockito.anyLong());

        L1AppConsumer consumer = new L1AppConsumer(l0SliceJobsDispatcher,
                l0SlicePatternSettings, processSettings, mqiService,
                mqiStatusService, appDataService,  errorAppender, appStatus);
        consumer.consumeMessages();

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
