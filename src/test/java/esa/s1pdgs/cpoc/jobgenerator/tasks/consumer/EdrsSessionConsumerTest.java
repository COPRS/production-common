package esa.s1pdgs.cpoc.jobgenerator.tasks.consumer;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFileRaw;
import esa.s1pdgs.cpoc.jobgenerator.service.EdrsSessionFileService;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus.JobStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.dispatcher.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL0Utils;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class EdrsSessionConsumerTest {

    @Mock
    private AbstractJobsDispatcher<EdrsSessionDto> jobsDispatcher;

    @Mock
    protected ProcessSettings processSettings;

    /**
     * Service for EDRS session file
     */
    @Mock
    private EdrsSessionFileService edrsSessionFileService;

    @Mock
    private GenericMqiService<EdrsSessionDto> mqiService;
    @Mock
    protected StatusService mqiStatusService;

    @Mock
    protected AbstractAppCatalogJobService<EdrsSessionDto> appDataService;
    /**
     * Application status
     */
    @Mock
    private AppStatus appStatus;

    @Mock
    private JobStatus jobStatus;

    private EdrsSessionDto dto1 = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1,
            EdrsSessionFileType.SESSION, "S1", "A");
    private EdrsSessionDto dto2 = new EdrsSessionDto("KEY_OBS_SESSION_1_2", 2,
            EdrsSessionFileType.SESSION, "S1", "A");
    private EdrsSessionDto dto3 = new EdrsSessionDto("KEY_OBS_SESSION_2_1", 1,
            EdrsSessionFileType.SESSION, "S1", "A");
    private EdrsSessionDto dto4 = new EdrsSessionDto("KEY_OBS_SESSION_2_2", 2,
            EdrsSessionFileType.SESSION, "S1", "A");
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

        // Mcokito
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

        // Mock the dispatcher
        Calendar start1 = Calendar.getInstance();
        start1.set(2017, Calendar.DECEMBER, 11, 14, 22, 37);
        start1.set(Calendar.MILLISECOND, 0);
        Calendar stop1 = Calendar.getInstance();
        stop1.set(2017, Calendar.DECEMBER, 11, 14, 42, 25);
        stop1.set(Calendar.MILLISECOND, 0);
        Mockito.doAnswer(i -> {
            return TestL0Utils.createEdrsSessionFileChannel1(true);
        }).when(edrsSessionFileService)
                .createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"));
        Mockito.doAnswer(i -> {
            return TestL0Utils.createEdrsSessionFileChannel2(true);
        }).when(edrsSessionFileService)
                .createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_2"));
        Mockito.doAnswer(i -> {
            EdrsSessionFile r = new EdrsSessionFile();
            r.setSessionId("SESSION_2");
            r.setStartTime(start1.getTime());
            r.setStopTime(stop1.getTime());
            r.setRawNames(Arrays.asList(
                    new EdrsSessionFileRaw("file1.raw", "file1.raw"),
                    new EdrsSessionFileRaw("file2.raw", "file2.raw")));
            return r;
        }).when(edrsSessionFileService)
                .createSessionFile(Mockito.eq("KEY_OBS_SESSION_2_1"));
        Mockito.doAnswer(i -> {
            EdrsSessionFile r = new EdrsSessionFile();
            r.setSessionId("SESSION_2");
            r.setStartTime(start1.getTime());
            r.setStopTime(stop1.getTime());
            r.setRawNames(Arrays.asList(
                    new EdrsSessionFileRaw("file1.raw", "file1.raw"),
                    new EdrsSessionFileRaw("file2.raw", "file2.raw")));
            return r;
        }).when(edrsSessionFileService)
                .createSessionFile(Mockito.eq("KEY_OBS_SESSION_2_2"));

        // Mock the MQI service
        doReturn(message1, message2, message3, message4).when(mqiService)
                .next();
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
            AppDataJobDto<LevelProductDto> ret = new AppDataJobDto<>();
            ret.setIdentifier(i.getArgument(0));
            ret.setState(i.getArgument(1));
            ret.setPod(i.getArgument(2));
            return ret;
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

    /**
     * Test that KAFKA consumer read a message
     * 
     * @throws Exception
     */
    @Test
    public void testReceiveSession() throws Exception {

        doReturn(message1, message3, message2, message4).when(mqiService)
                .next();

        EdrsSessionConsumer edrsSessionsConsumer =
                new EdrsSessionConsumer(jobsDispatcher, processSettings,
                        mqiService, edrsSessionFileService, mqiStatusService,
                        appDataService, appStatus);

        // Job<EdrsSession> job = new Job<EdrsSession>(session.getSessionId(),
        // session.getStartTime(), session.getStartTime(), session);
        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(1L));
        verify(appStatus, times(1)).setWaiting();

        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        verify(appStatus, times(1)).setProcessing(Mockito.eq(3L));
        verify(appStatus, times(2)).setWaiting();

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
        EdrsSessionConsumer edrsSessionsConsumer =
                new EdrsSessionConsumer(jobsDispatcher, processSettings,
                        mqiService, edrsSessionFileService, mqiStatusService,
                        appDataService, appStatus);
        doReturn(new GenericMessageDto<EdrsSessionDto>(1, "",
                new EdrsSessionDto("KEY_OBS_SESSION_2_2", 2,
                        EdrsSessionFileType.RAW, "S1", "A"))).when(mqiService)
                                .next();
        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(edrsSessionFileService, never())
                .createSessionFile(Mockito.anyString());
        Mockito.verify(jobsDispatcher, never()).dispatch(Mockito.any());
    }

    @Test
    public void testReceivedSameMessageTwice() throws Exception {
        EdrsSessionConsumer edrsSessionsConsumer =
                new EdrsSessionConsumer(jobsDispatcher, processSettings,
                        mqiService, edrsSessionFileService, mqiStatusService,
                        appDataService, appStatus);
        doReturn(message1, message1).when(mqiService).next();

        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        Mockito.verify(edrsSessionFileService, times(1))
                .createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"));

        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        Mockito.verify(edrsSessionFileService, times(2))
                .createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"));
    }

    @Test
    public void testReceivedInvalidProductChannel() throws Exception {
        EdrsSessionConsumer edrsSessionsConsumer =
                new EdrsSessionConsumer(jobsDispatcher, processSettings,
                        mqiService, edrsSessionFileService, mqiStatusService,
                        appDataService, appStatus);
        dto1.setChannelId(3);
        doReturn(message1).when(mqiService).next();

        edrsSessionsConsumer.consumeMessages();
        Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
        Mockito.verify(edrsSessionFileService, Mockito.never())
                .createSessionFile(Mockito.any());
    }

}
