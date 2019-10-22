package esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.jobgenerator.config.L0SegmentAppProperties;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus.JobStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class L0SegmentAppConsumerTest {

	@Mock
	private L0SegmentAppProperties appProperties;

	@Mock
	private ProcessSettings processSettings;

	@Mock
	private AbstractJobsDispatcher<ProductDto> jobsDispatcher;

	@Mock
	private GenericMqiClient mqiService;

	@Mock
	private StatusService mqiStatusService;

	@Mock
	private AppCatalogJobClient appDataService;

	@Mock
	private AppStatus appStatus;

	@Mock
	private JobStatus jobStatus;

	private List<GenericMessageDto<ProductDto>> messages;

	private L0SegmentAppConsumer consumer;

	private ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL;

	@Before
	public void init() throws AbstractCodedException {
		MockitoAnnotations.initMocks(this);

		initMessages();
		mockProperties();
		mockJobDispatcher();
		mockMqiService();
		mockAppDataService();
		mockAppStatus();

		consumer = new L0SegmentAppConsumer(jobsDispatcher, appProperties, processSettings, mqiService,
				mqiStatusService, appDataService, errorAppender, appStatus, 0, 0);
		consumer.setTaskForFunctionalLog(processSettings.getLevel().name() + "JobGeneration");
	}

	public void mockProperties() {
		HashMap<String, Integer> groups = new HashMap<>();
		groups.put("missionId", 1);
		groups.put("satelliteId", 2);
		groups.put("acquisition", 4);
		groups.put("polarisation", 5);
		groups.put("startTime", 6);
		groups.put("stopTime", 7);
		groups.put("datatakeId", 9);
		doReturn(groups).when(appProperties).getNameRegexpGroups();
		doReturn(
				"^([0-9a-z]{2})([0-9a-z]{1})_(([0-9a-z]{2})_RAW__0S([0-9a-z_]{2}))_([0-9a-z]{15})_([0-9a-z]{15})_([0-9a-z_]{6})_([0-9a-z_]{6})\\w{1,}\\.SAFE(/.*)?$")
						.when(appProperties).getNameRegexpPattern();

		doReturn(ApplicationLevel.L0_SEGMENT).when(processSettings).getLevel();
		doReturn("hostname").when(processSettings).getHostname();
	}

	public void initMessages() {
		messages = new ArrayList<>();
		GenericMessageDto<ProductDto> message1 = new GenericMessageDto<ProductDto>(1, "topic1",
				new ProductDto("S1B_IW_RAW__0SHV_20171218T094703_20171218T094735_008772_00F9CD_EB01.SAFE",
						"S1B_IW_RAW__0SHV_20171218T094703_20171218T094735_008772_00F9CD_EB01.SAFE",
						ProductFamily.L0_SEGMENT, "FAST"));
		messages.add(message1);
		GenericMessageDto<ProductDto> message2 = new GenericMessageDto<ProductDto>(2, "topic1",
				new ProductDto("S1B_IW_RAW__0SSV_20171218T090732_20171218T090732_008771_00F9CA_C40B.SAFE",
						"S1B_IW_RAW__0SSV_20171218T090732_20171218T090732_008771_00F9CA_C40B.SAFE",
						ProductFamily.L0_SEGMENT, "FAST"));
		messages.add(message2);
		GenericMessageDto<ProductDto> message3 = new GenericMessageDto<ProductDto>(1, "topic1",
				new ProductDto("S1B_IW_RAW__0SHH_20171218T094703_20171218T094735_008772_00F9CD_EB01.SAFE",
						"S1B_IW_RAW__0SHH_20171218T094703_20171218T094735_008772_00F9CD_EB01.SAFE",
						ProductFamily.L0_SEGMENT, "FAST"));
		messages.add(message3);
	}

	public void mockJobDispatcher() throws AbstractCodedException {
		doNothing().when(jobsDispatcher).dispatch(any());
	}

	public void mockMqiService() throws AbstractCodedException {
		doReturn(messages.get(0), messages.get(1), messages.get(2)).when(mqiService).next(Mockito.any());
		doReturn(true).when(mqiService).ack(any(), Mockito.any());
	}

	public void mockAppDataService() throws AbstractCodedException {
		doReturn(new ArrayList<>()).when(appDataService).findByMessagesId(anyLong());
		doReturn(new ArrayList<>()).when(appDataService).findByProductDataTakeId(anyString());
		Mockito.doAnswer(i -> {
			return i.getArgument(0);
		}).when(appDataService).newJob(Mockito.any());
		Mockito.doAnswer(i -> {
			return i.getArgument(1);
		}).when(appDataService).patchJob(Mockito.anyLong(), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
				Mockito.anyBoolean());
	}

	public void mockAppStatus() {
		doNothing().when(appStatus).setWaiting();
		doNothing().when(appStatus).setProcessing(anyLong());
		doNothing().when(appStatus).setError(anyString());
		doReturn(jobStatus).when(appStatus).getStatus();
		doReturn(false).when(jobStatus).isStopping();
	}

	@Test
	public void testGetTaskForFunctionalLog() {
		assertEquals("L0_SEGMENTJobGeneration", consumer.getTaskForFunctionalLog());
	}

	@Test
	public void testBuildJobNew() throws AbstractCodedException {
		AppDataJob expectedData = new AppDataJob();
		expectedData.setLevel(processSettings.getLevel());
		expectedData.setPod(processSettings.getHostname());
		expectedData.getMessages().add(messages.get(0));
		AppDataJobProduct productDto = new AppDataJobProduct();
		productDto.setAcquisition("IW");
		productDto.setMissionId("S1");
		productDto.setDataTakeId("00F9CD");
		productDto.setProductName("l0_segments_for_00F9CD");
		productDto.setProcessMode("FAST");
		productDto.setSatelliteId("B");
		expectedData.setProduct(productDto);

		AppDataJob result = consumer.buildJob(messages.get(0));
		assertEquals(expectedData, result);
		verify(appDataService, times(1)).findByMessagesId(eq(1L));
		verify(appDataService, times(1)).findByProductDataTakeId(eq("00F9CD"));
		verify(appDataService, times(1)).newJob(eq(expectedData));
	}

	@Test
	public void testConsumeWhenNoMessage() throws AbstractCodedException {
		consumer.onMessage(null);

		verifyZeroInteractions(jobsDispatcher, appDataService);
		verify(appStatus, never()).setProcessing(Mockito.eq(2L));
		verify(appStatus, times(1)).setWaiting();
	}

	@Test
	public void testConsumeWhenNewJob() throws AbstractCodedException {
		AppDataJob expectedData = new AppDataJob();
		expectedData.setLevel(processSettings.getLevel());
		expectedData.setPod(processSettings.getHostname());
		expectedData.setState(AppDataJobState.DISPATCHING);
		expectedData.getMessages().add(messages.get(0));
		AppDataJobProduct productDto = new AppDataJobProduct();
		productDto.setAcquisition("IW");
		productDto.setMissionId("S1");
		productDto.setDataTakeId("00F9CD");
		productDto.setProductName("l0_segments_for_00F9CD");
		productDto.setProcessMode("FAST");
		productDto.setSatelliteId("B");
		expectedData.setProduct(productDto);

		consumer.onMessage(messages.get(0));

		verify(appDataService, times(1)).findByMessagesId(eq(1L));
		verify(appDataService, times(1)).findByProductDataTakeId(eq("00F9CD"));
		verify(appDataService, times(1)).newJob(any());
		verify(appDataService, times(1)).patchJob(anyLong(), any(), eq(false), eq(false), eq(false));
		verify(jobsDispatcher).dispatch(eq(expectedData));
	}
}
