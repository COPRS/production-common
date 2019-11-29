package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.l0app;

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
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.utils.TestL0Utils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class L0AppConsumerTest {

    @Mock
    private AbstractJobsDispatcher<CatalogEvent> jobsDispatcher;

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
    private Status jobStatus;
    
    @Mock
    private MetadataClient metadataClient;

    private ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL ;

    // TODO FIXME
    
    private CatalogEvent ingestionEvent1 = TestL0Utils.newCatalogEvent("KEY_OBS_SESSION_1_1", "/path/of/inbox", 1,
            EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
    private CatalogEvent ingestionEvent2 = TestL0Utils.newCatalogEvent("KEY_OBS_SESSION_1_2", "/path/of/inbox", 2,
            EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
    private CatalogEvent ingestionEvent3 = TestL0Utils.newCatalogEvent("KEY_OBS_SESSION_2_1", "/path/of/inbox", 1,
            EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
    private CatalogEvent ingestionEvent4 = TestL0Utils.newCatalogEvent("KEY_OBS_SESSION_2_2", "/path/of/inbox", 2,
            EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
    private GenericMessageDto<CatalogEvent> message1 =
            new GenericMessageDto<CatalogEvent>(1, "",ingestionEvent1);
    private GenericMessageDto<CatalogEvent> message2 =
            new GenericMessageDto<CatalogEvent>(2, "", ingestionEvent2);
    private GenericMessageDto<CatalogEvent> message3 =
            new GenericMessageDto<CatalogEvent>(3, "", ingestionEvent3);
    private GenericMessageDto<CatalogEvent> message4 =
            new GenericMessageDto<CatalogEvent>(4, "", ingestionEvent4);

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
        Mockito.doAnswer(i -> true).when(jobsDispatcher).dispatch(Mockito.any());

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

    /**
     * Test that KAFKA consumer read a message
     * 
     * @throws Exception
     */
    @Test
    public void testReceiveSession() throws Exception {

        final L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataClient, 0, 0);

        // Job<EdrsSession> job = new Job<EdrsSession>(session.getSessionId(),
        // session.getStartTime(), session.getStartTime(), session);
        edrsSessionsConsumer.onMessage(message1);
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(1L));
        verify(appStatus, times(2)).setWaiting();

        edrsSessionsConsumer.onMessage(message2);
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(2L));
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
        final L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataClient, 0, 0);
        final GenericMessageDto<CatalogEvent> mqiMessage= new GenericMessageDto<CatalogEvent>(1, "",
                TestL0Utils.newCatalogEvent("KEY_OBS_SESSION_2_2", "/path/of/inbox", 2,
                        EdrsSessionFileType.RAW, "S1", "A", "WILE", "sessionId"));
        
        
        edrsSessionsConsumer.onMessage(mqiMessage);
        Mockito.verify(jobsDispatcher, never()).dispatch(Mockito.any());
    }

	@Test
    public void testReceivedSameMessageTwice() throws Exception {
        final L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataClient, 0, 0);

        System.out.println(Integer.MAX_VALUE);
        edrsSessionsConsumer.onMessage(message1);
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());

        edrsSessionsConsumer.onMessage(message1);
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
    }

    @Test
    public void testReceivedInvalidProductChannel() throws Exception {
        final L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataClient, 0, 0);
        ingestionEvent1.setChannelId(3);

        edrsSessionsConsumer.onMessage(message1);
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
    }

    @Test
    public void testBuildWhenMessageIdExistSameHostname()
            throws AbstractCodedException {

        final CatalogEvent ingestionEvent = TestL0Utils.newCatalogEvent("KEY_OBS_SESSION_1_1", "/path/of/inbox", 1,
                EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
        final GenericMessageDto<CatalogEvent> message =
                new GenericMessageDto<CatalogEvent>(123, "", ingestionEvent);

        final AppDataJob expected = TestL0Utils.buildAppDataEdrsSession(false);

        doReturn(Arrays.asList(expected)).when(appDataService)
                .findByMessagesId(Mockito.anyLong());

        final L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataClient, 0, 0);

        final AppDataJob result = edrsSessionsConsumer.buildJob(message);
        verify(appDataService, never()).patchJob(Mockito.anyLong(),
                Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                Mockito.anyBoolean());
        assertEquals(expected, result);
    }

    @Test
    public void testBuildWhenMessageIdExistDifferentHostname()
            throws AbstractCodedException {

        final CatalogEvent ingestionEvent = TestL0Utils.newCatalogEvent("KEY_OBS_SESSION_1_1", "/path/of/inbox", 1,
                EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
        final GenericMessageDto<CatalogEvent> message =
                new GenericMessageDto<CatalogEvent>(123, "", ingestionEvent);

        final AppDataJob expected =
                TestL0Utils.buildAppDataEdrsSession(true);
        final AppDataJob returned =
                TestL0Utils.buildAppDataEdrsSession(true);
        returned.setPod("other-pod");

        doReturn(Arrays.asList(returned)).when(appDataService)
                .findByMessagesId(Mockito.anyLong());

        final L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataClient, 0, 0);

        final AppDataJob result =
                edrsSessionsConsumer.buildJob(message);
        verify(appDataService, times(1)).patchJob(Mockito.eq(123L),
                Mockito.any(), Mockito.eq(false), Mockito.eq(false),
                Mockito.eq(false));
        assertEquals(expected, result);
    }

    @Test
    public void testBuildWhenMessageIdNotExistNewRaw()
            throws AbstractCodedException {
    	final CatalogEvent ingestionEvent = TestL0Utils.newCatalogEvent("obs1", "/path/of/inbox", 1,
                EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
        final GenericMessageDto<CatalogEvent> message =
                new GenericMessageDto<CatalogEvent>(123, "", ingestionEvent);

        final AppDataJob expected =
                TestL0Utils.buildAppDataEdrsSession(true);
        final AppDataJob returned =
                TestL0Utils.buildAppDataEdrsSessionWithRaw2(true);

        doReturn(null).when(appDataService)
                .findByMessagesId(Mockito.anyLong());
        doReturn(Arrays.asList(returned)).when(appDataService)
                .findByProductSessionId(Mockito.anyString());

        final L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataClient, 0, 0);

        final AppDataJob result =
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
        final CatalogEvent ingestionEvent = TestL0Utils.newCatalogEvent("obs1", "/path/of/inbox", 1,
                EdrsSessionFileType.SESSION, "S1", "A", "WILE", "sessionId");
        final GenericMessageDto<CatalogEvent> message =
                new GenericMessageDto<CatalogEvent>(123, "", ingestionEvent);

        final AppDataJob expected =
                TestL0Utils.buildAppDataEdrsSession(true);
        final AppDataJob returned =
                TestL0Utils.buildAppDataEdrsSession(true);
        returned.setPod("other-pod");

        doReturn(null).when(appDataService)
                .findByMessagesId(Mockito.anyLong());
        doReturn(Arrays.asList(returned)).when(appDataService)
                .findByProductSessionId(Mockito.anyString());

        final L0AppConsumer edrsSessionsConsumer = new L0AppConsumer(jobsDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                errorAppender, appStatus, metadataClient, 0, 0);

        final AppDataJob<?> result =
                edrsSessionsConsumer.buildJob(message);
        verify(appDataService, times(1)).patchJob(Mockito.eq(123L),
                Mockito.any(), Mockito.eq(false), Mockito.eq(false),
                Mockito.eq(false));
        assertTrue(result.getMessages().size() == 2);
        assertEquals(expected.getPod(), result.getPod());
    }

}
