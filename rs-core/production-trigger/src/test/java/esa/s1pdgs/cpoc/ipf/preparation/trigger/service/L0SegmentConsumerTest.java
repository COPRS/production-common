package esa.s1pdgs.cpoc.ipf.preparation.trigger.service;

public class L0SegmentConsumerTest {
	
	// FIXME
//
//	@Mock
//	private L0SegmentAppProperties appProperties;
//
//	@Mock
//	private ProcessSettings processSettings;
//
//	@Mock
//	private GenericMqiClient mqiService;
//
//	@Mock
//	private StatusService mqiStatusService;
//
//	@Mock
//	private AppCatalogJobClient appDataService;
//
//	@Mock
//	private AppStatus appStatus;
//
//	@Mock
//	private Status jobStatus;
//
//	private List<GenericMessageDto<CatalogEvent>> messages;
//
//	private L0SegmentConsumer consumer;
//
//	private ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL;
//
//	@Before
//	public void init() throws AbstractCodedException {
//		MockitoAnnotations.initMocks(this);
//
//		initMessages();
//		mockProperties();
//		mockJobDispatcher();
//		mockMqiService();
//		mockAppDataService();
//		mockAppStatus();
//
//		consumer = new L0SegmentConsumer(
//				jobsDispatcher, 
//				appProperties, 
//				processSettings, mqiService,
//				mqiStatusService, appDataService, errorAppender, appStatus, 0, 0);
//		consumer.setTaskForFunctionalLog(processSettings.getLevel().name() + "JobGeneration");
//	}
//
//	public void mockProperties() {
//		doReturn(ApplicationLevel.L0_SEGMENT).when(processSettings).getLevel();
//		doReturn("hostname").when(processSettings).getHostname();
//	}
//
//	public void initMessages() {
//		messages = new ArrayList<>();
//		final GenericMessageDto<CatalogEvent> message1 = new GenericMessageDto<CatalogEvent>(1, "topic1",
//				TestL0Utils.newSegmentCatalogEvent("S1B_IW_RAW__0SHV_20171218T094703_20171218T094735_008772_00F9CD_EB01.SAFE",
//						"S1B_IW_RAW__0SHV_20171218T094703_20171218T094735_008772_00F9CD_EB01.SAFE",
//						ProductFamily.L0_SEGMENT, "FAST"));
//		messages.add(message1);
//		final GenericMessageDto<CatalogEvent> message2 = new GenericMessageDto<CatalogEvent>(2, "topic1",
//				TestL0Utils.newSegmentCatalogEvent("S1B_IW_RAW__0SSV_20171218T090732_20171218T090732_008771_00F9CA_C40B.SAFE",
//						"S1B_IW_RAW__0SSV_20171218T090732_20171218T090732_008771_00F9CA_C40B.SAFE",
//						ProductFamily.L0_SEGMENT, "FAST"));
//		messages.add(message2);
//		final GenericMessageDto<CatalogEvent> message3 = new GenericMessageDto<CatalogEvent>(1, "topic1",
//				TestL0Utils.newSegmentCatalogEvent("S1B_IW_RAW__0SHH_20171218T094703_20171218T094735_008772_00F9CD_EB01.SAFE",
//						"S1B_IW_RAW__0SHH_20171218T094703_20171218T094735_008772_00F9CD_EB01.SAFE",
//						ProductFamily.L0_SEGMENT, "FAST"));
//		messages.add(message3);
//	}
//
//	public void mockJobDispatcher() throws AbstractCodedException {
//		doNothing().when(jobsDispatcher).dispatch(any());
//	}
//
//	public void mockMqiService() throws AbstractCodedException {
//		doReturn(messages.get(0), messages.get(1), messages.get(2)).when(mqiService).next(Mockito.any());
//		doReturn(true).when(mqiService).ack(any(), Mockito.any());
//	}
//
//	public void mockAppDataService() throws AbstractCodedException {
//		doReturn(new ArrayList<>()).when(appDataService).findByMessagesId(anyLong());
//		doReturn(new ArrayList<>()).when(appDataService).findByProductDataTakeId(anyString());
//		Mockito.doAnswer(i -> {
//			return i.getArgument(0);
//		}).when(appDataService).newJob(Mockito.any());
//		Mockito.doAnswer(i -> {
//			return i.getArgument(1);
//		}).when(appDataService).patchJob(Mockito.anyLong(), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(),
//				Mockito.anyBoolean());
//	}
//
//	public void mockAppStatus() {
//		doNothing().when(appStatus).setWaiting();
//		doNothing().when(appStatus).setProcessing(anyLong());
//		doNothing().when(appStatus).setError(anyString());
//		doReturn(jobStatus).when(appStatus).getStatus();
//		doReturn(false).when(jobStatus).isStopping();
//	}
//
//	@Test
//	public void testBuildJobNew() throws AbstractCodedException {
//		final AppDataJob expectedData = new AppDataJob();
//		expectedData.setLevel(processSettings.getLevel());
//		expectedData.setPod(processSettings.getHostname());
//		expectedData.getMessages().add(messages.get(0));
//		final AppDataJobProduct productDto = new AppDataJobProduct();
//		productDto.setAcquisition("IW");
//		productDto.setMissionId("S1");
//		productDto.setDataTakeId("00F9CD");
//		productDto.setProductName("l0_segments_for_00F9CD");
//		productDto.setProcessMode("FAST");
//		productDto.setSatelliteId("B");
//		expectedData.setProduct(productDto);
//
//		final AppDataJob result = consumer.buildJob(messages.get(0));
//		assertEquals(expectedData, result);
//		verify(appDataService, times(1)).findByMessagesId(eq(1L));
//		verify(appDataService, times(1)).findByProductDataTakeId(eq("00F9CD"));
//		verify(appDataService, times(1)).newJob(Mockito.any());
//	}
//
//	@Test
//	public void testConsumeWhenNoMessage() throws AbstractCodedException {
//		consumer.onMessage(null);
//
//		verifyZeroInteractions(jobsDispatcher, appDataService);
//		verify(appStatus, never()).setProcessing(Mockito.eq(2L));
//		verify(appStatus, times(1)).setWaiting();
//	}
//
//	@Test
//	public void testConsumeWhenNewJob() throws AbstractCodedException {
//		final AppDataJob expectedData = new AppDataJob();
//		expectedData.setLevel(processSettings.getLevel());
//		expectedData.setPod(processSettings.getHostname());
//		expectedData.setState(AppDataJobState.DISPATCHING);
//		expectedData.getMessages().add(messages.get(0));
//		final AppDataJobProduct productDto = new AppDataJobProduct();
//		productDto.setAcquisition("IW");
//		productDto.setMissionId("S1");
//		productDto.setDataTakeId("00F9CD");
//		productDto.setProductName("l0_segments_for_00F9CD");
//		productDto.setProcessMode("FAST");
//		productDto.setSatelliteId("B");
//		expectedData.setProduct(productDto);
//
//		consumer.onMessage(messages.get(0));
//
//		verify(appDataService, times(1)).findByMessagesId(eq(1L));
//		verify(appDataService, times(1)).findByProductDataTakeId(eq("00F9CD"));
//		verify(appDataService, times(1)).newJob(any());
//		verify(appDataService, times(1)).patchJob(anyLong(), any(), eq(false), eq(false), eq(false));
//		verify(jobsDispatcher).dispatch(Mockito.any());
//	}
}
