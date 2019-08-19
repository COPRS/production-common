package esa.s1pdgs.cpoc.jobgenerator.tasks.l0app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus.JobStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL0Utils;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class L0AppConsumerTest {

    @Mock
    private AbstractJobsDispatcher<EdrsSessionDto> jobsDispatcher;

    @Mock
    protected ProcessSettings processSettings;

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
    private JobStatus jobStatus;
    
    @Mock
    private MetadataService metadataService;

    private ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL ;

    private EdrsSessionDto dto1 = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1,
            EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
    private EdrsSessionDto dto2 = new EdrsSessionDto("KEY_OBS_SESSION_1_2", 2,
            EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
    private EdrsSessionDto dto3 = new EdrsSessionDto("KEY_OBS_SESSION_2_1", 1,
            EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
    private EdrsSessionDto dto4 = new EdrsSessionDto("KEY_OBS_SESSION_2_2", 2,
            EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
    private GenericMessageDto<EdrsSessionDto> message1 =
            new GenericMessageDto<EdrsSessionDto>(1, "", dto1);
    private GenericMessageDto<EdrsSessionDto> message2 =
            new GenericMessageDto<EdrsSessionDto>(2, "", dto2);
    private GenericMessageDto<EdrsSessionDto> message3 =
            new GenericMessageDto<EdrsSessionDto>(3, "", dto3);
    private GenericMessageDto<EdrsSessionDto> message4 =
            new GenericMessageDto<EdrsSessionDto>(4, "", dto4);

    /**
     * Test set up
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        // Mockito
        MockitoAnnotations.initMocks(this);

        this.mockProcessSettings();

        // Mock the dispatcher
        Mockito.doAnswer(i -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
            return true;
        }).when(jobsDispatcher).dispatch(Mockito.any());

        // Mock the MQI service
        doReturn(message1, message2, message3, message4).when(mqiService)
                .next(Mockito.any());
        doReturn(true).when(mqiService).ack(Mockito.any(), Mockito.any());

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
        
        // Mock metadata service
        Mockito.doAnswer(i -> {
        	return new EdrsSessionMetadata("name", "type", "kobs", "start", "stop", "vstart", "vstop", "mission", "satellite", "station",
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
        }).when(metadataService).getEdrsSession(Mockito.anyString(), Mockito.anyString());
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

    /**
     * Test that KAFKA consumer read a message
     * 
     * @throws Exception
     */
    @Test
    public void testReceiveSession() throws Exception {

        doReturn(message1, message3, message2, message4).when(mqiService)
                .next(Mockito.any());

        L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataService);

        // Job<EdrsSession> job = new Job<EdrsSession>(session.getSessionId(),
        // session.getStartTime(), session.getStartTime(), session);
        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(1L));
        verify(appStatus, times(2)).setWaiting();

        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(3L));
        verify(appStatus, times(4)).setWaiting();

        // TODO
        /*
         * edrsSessionsConsumer.consumeMessages();
         * Mockito.verify(jobsDispatcher, times(1)).dispatch(Mockito.any());
         * verify(appStatus, times(1)).setProcessing(Mockito.eq(2L));
         * verify(appStatus, times(3)).setWaiting();
         * edrsSessionsConsumer.consumeMessages();
         * Mockito.verify(jobsDispatcher, times(2)).dispatch(Mockito.any());
         * verify(appStatus, times(1)).setProcessing(Mockito.eq(4L));
         * verify(appStatus, times(4)).setWaiting();
         */
    }

    /**
     * Test that KAFKA consumer read a message
     * 
     * @throws Exception
     */
    @Test
    public void testReceiveRaw() throws Exception {
        L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataService);
        doReturn(new GenericMessageDto<EdrsSessionDto>(1, "",
                new EdrsSessionDto("KEY_OBS_SESSION_2_2", 2,
                        EdrsSessionFileType.RAW, "S1", "A", "WILE", "sessionId"))).when(mqiService)
                                .next(Mockito.any());
        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, never()).dispatch(Mockito.any());
    }

    @Test
    public void testReceivedSameMessageTwice() throws Exception {
        L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataService);
        doReturn(message1, message1).when(mqiService).next(Mockito.any());

        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());

        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
    }

    @Test
    public void testReceivedInvalidProductChannel() throws Exception {
        L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataService);
        dto1.setChannelId(3);
        doReturn(message1).when(mqiService).next(Mockito.any());

        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
    }

    @Test
    public void testBuildWhenMessageIdExistSameHostname()
            throws AbstractCodedException {

        EdrsSessionDto dto = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1,
                EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
        GenericMessageDto<EdrsSessionDto> message =
                new GenericMessageDto<EdrsSessionDto>(123, "", dto);

        AppDataJobDto<EdrsSessionDto> expected = TestL0Utils.buildAppDataEdrsSession(false);

        doReturn(Arrays.asList(expected)).when(appDataService)
                .findByMessagesIdentifier(Mockito.anyLong());

        L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataService);

        AppDataJobDto<EdrsSessionDto> result = edrsSessionsConsumer.buildJob(message);
        verify(appDataService, never()).patchJob(Mockito.anyLong(),
                Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());
        assertEquals(expected, result);
    }

    @Test
    public void testBuildWhenMessageIdExistDifferentHostname()
            throws AbstractCodedException {

        EdrsSessionDto dto = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1,
                EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
        GenericMessageDto<EdrsSessionDto> message =
                new GenericMessageDto<EdrsSessionDto>(123, "", dto);

        AppDataJobDto<EdrsSessionDto> expected =
                TestL0Utils.buildAppDataEdrsSession(true);
        AppDataJobDto<EdrsSessionDto> returned =
                TestL0Utils.buildAppDataEdrsSession(true);
        returned.setPod("other-pod");

        doReturn(Arrays.asList(returned)).when(appDataService)
                .findByMessagesIdentifier(Mockito.anyLong());

        L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataService);

        AppDataJobDto<EdrsSessionDto> result =
                edrsSessionsConsumer.buildJob(message);
        verify(appDataService, times(1)).patchJob(Mockito.eq(123L),
                Mockito.any(), Mockito.eq(false), Mockito.eq(false),
                Mockito.eq(false));
        assertEquals(expected, result);
    }

    @Test
    public void testBuildWhenMessageIdNotExistNewRaw()
            throws AbstractCodedException {
    	EdrsSessionDto dto = new EdrsSessionDto("obs1", 1,
                EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
        GenericMessageDto<EdrsSessionDto> message =
                new GenericMessageDto<EdrsSessionDto>(123, "", dto);

        AppDataJobDto<EdrsSessionDto> expected =
                TestL0Utils.buildAppDataEdrsSession(true);
        AppDataJobDto<EdrsSessionDto> returned =
                TestL0Utils.buildAppDataEdrsSessionWithRaw2(true);

        doReturn(null).when(appDataService)
                .findByMessagesIdentifier(Mockito.anyLong());
        doReturn(Arrays.asList(returned)).when(appDataService)
                .findByProductSessionId(Mockito.anyString());

        L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataService);

        AppDataJobDto<EdrsSessionDto> result =
                edrsSessionsConsumer.buildJob(message);
        verify(appDataService, times(1)).patchJob(Mockito.eq(123L),
                Mockito.any(), Mockito.eq(true), Mockito.eq(true),
                Mockito.eq(false));
        assertTrue(result.getMessages().size() == 2);
        assertEquals(expected.getProduct().getRaws2(),
                result.getProduct().getRaws2());
        assertEquals(expected.getProduct().getRaws1(),
                result.getProduct().getRaws1());
        assertEquals(expected.getPod(), result.getPod());
    }

    @Test
    public void testBuildWhenMessageIdNotExistHostnameDifeerentAllRaw()
            throws AbstractCodedException {
        EdrsSessionDto dto = new EdrsSessionDto("obs1", 1,
                EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
        GenericMessageDto<EdrsSessionDto> message =
                new GenericMessageDto<EdrsSessionDto>(123, "", dto);

        AppDataJobDto<EdrsSessionDto> expected =
                TestL0Utils.buildAppDataEdrsSession(true);
        AppDataJobDto<EdrsSessionDto> returned =
                TestL0Utils.buildAppDataEdrsSession(true);
        returned.setPod("other-pod");

        doReturn(null).when(appDataService)
                .findByMessagesIdentifier(Mockito.anyLong());
        doReturn(Arrays.asList(returned)).when(appDataService)
                .findByProductSessionId(Mockito.anyString());

        L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataService);

        AppDataJobDto<EdrsSessionDto> result =
                edrsSessionsConsumer.buildJob(message);
        verify(appDataService, times(1)).patchJob(Mockito.eq(123L),
                Mockito.any(), Mockito.eq(false), Mockito.eq(false),
                Mockito.eq(false));
        assertTrue(result.getMessages().size() == 2);
        assertEquals(expected.getPod(), result.getPod());
    }

}
