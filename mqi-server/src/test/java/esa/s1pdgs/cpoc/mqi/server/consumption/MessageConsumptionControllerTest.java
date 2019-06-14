package esa.s1pdgs.cpoc.mqi.server.consumption;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiSendMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.common.errors.processing.StatusProcessingApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryConsumptionProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryProperties;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatus;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@Ignore
public class MessageConsumptionControllerTest {

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, false,
            GenericKafkaUtils.TOPIC_ERROR, GenericKafkaUtils.TOPIC_L0_JOBS,
            GenericKafkaUtils.TOPIC_EDRS_SESSIONS);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private ApplicationProperties appProperties;

    @Autowired
    private KafkaProperties kafkaProperties;

    /**
     * Service for AUXILIARY_FILES
     */
    @Mock
    private GenericAppCatalogMqiService<AuxiliaryFileDto> persistAuxiliaryFilesService;

    /**
     * Service for AUXILIARY_FILES
     */
    @Mock
    private GenericAppCatalogMqiService<EdrsSessionDto> persistEdrsSessionsService;

    /**
     * Service for AUXILIARY_FILES
     */
    @Mock
    private GenericAppCatalogMqiService<LevelJobDto> persistLevelJobsService;

    /**
     * Service for AUXILIARY_FILES
     */
    @Mock
    private GenericAppCatalogMqiService<LevelProductDto> persistLevelProductsService;

    /**
     * Service for AUXILIARY_FILES
     */
    @Mock
    private GenericAppCatalogMqiService<LevelReportDto> persistLevelReportsService;

    /**
     * Service for AUXILIARY_FILES
     */
    @Mock
    private GenericAppCatalogMqiService<LevelSegmentDto> persistLevelSegmentsService;
    
    @Mock
    private GenericAppCatalogMqiService<CompressionJobDto> persistCompressedJobService;

    /**
     * Service for Errors
     */
    @Mock
    private GenericAppCatalogMqiService<ErrorDto> persistErrorsService;

    /**
     * 
     */
    @Mock
    private OtherApplicationService otherService;

    /**
     * Application status
     */
    @Autowired
    protected AppStatus appStatus;

    @Autowired
    private MessageConsumptionController autoManager;

    private MessageConsumptionController manager;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        manager = new MessageConsumptionController(appProperties,
                kafkaProperties, persistAuxiliaryFilesService,
                persistEdrsSessionsService, persistLevelJobsService,
                persistLevelProductsService, persistLevelReportsService,
                persistLevelSegmentsService,persistCompressedJobService, otherService, appStatus);

        doReturn("pod-name").when(appProperties).getHostname();

        startManagerWithAllConsumers();
    }

    /**
     * Build the "manager" which is a message controller with all categories
     * activated
     */
    private void startManagerWithAllConsumers() {
        Map<String, Integer> auxTopicsWithPriority = new HashMap<>();
        auxTopicsWithPriority.put("topic", 100);
        auxTopicsWithPriority.put("topic-other", 10);
        Map<String, Integer> edrsTopicsWithPriority = new HashMap<>();
        edrsTopicsWithPriority.put("topic", 10);
        Map<String, Integer> lProdTopicsWithPriority = new HashMap<>();
        lProdTopicsWithPriority.put("topic", 100);
        Map<String, Integer> lJobTopicsWithPriority = new HashMap<>();
        lJobTopicsWithPriority.put("topic", 10);
        Map<String, Integer> lRepTopicsWithPriority = new HashMap<>();
        lRepTopicsWithPriority.put("topic", 100);
        lRepTopicsWithPriority.put("another-topic", 10);
        Map<String, Integer> lSegTopicsWithPriority = new HashMap<>();
        lSegTopicsWithPriority.put("topic", 100);
        Map<String, Integer> lErrorsTopics = new HashMap<>();
        lErrorsTopics.put("topic", 100);
        Map<ProductCategory, ProductCategoryProperties> map = new HashMap<>();
        ProductCategoryConsumptionProperties prodCatAux =
                new ProductCategoryConsumptionProperties(true);
        prodCatAux.setTopicsWithPriority(auxTopicsWithPriority);
        map.put(ProductCategory.AUXILIARY_FILES,
                new ProductCategoryProperties(prodCatAux, null));
        ProductCategoryConsumptionProperties prodCatProd =
                new ProductCategoryConsumptionProperties(true);
        prodCatProd.setTopicsWithPriority(lProdTopicsWithPriority);
        map.put(ProductCategory.LEVEL_PRODUCTS,
                new ProductCategoryProperties(prodCatProd, null));
        ProductCategoryConsumptionProperties prodCatJob =
                new ProductCategoryConsumptionProperties(true);
        prodCatJob.setTopicsWithPriority(lJobTopicsWithPriority);
        map.put(ProductCategory.LEVEL_JOBS,
                new ProductCategoryProperties(prodCatJob, null));
        ProductCategoryConsumptionProperties prodCatEDRS =
                new ProductCategoryConsumptionProperties(true);
        prodCatEDRS.setTopicsWithPriority(edrsTopicsWithPriority);
        map.put(ProductCategory.EDRS_SESSIONS,
                new ProductCategoryProperties(prodCatEDRS, null));
        ProductCategoryConsumptionProperties prodCatRep =
                new ProductCategoryConsumptionProperties(true);
        prodCatRep.setTopicsWithPriority(lRepTopicsWithPriority);
        map.put(ProductCategory.LEVEL_REPORTS,
                new ProductCategoryProperties(prodCatRep, null));
        ProductCategoryConsumptionProperties prodCatSeg =
                new ProductCategoryConsumptionProperties(true);
        prodCatSeg.setTopicsWithPriority(lSegTopicsWithPriority);
        map.put(ProductCategory.LEVEL_SEGMENTS,
                new ProductCategoryProperties(prodCatSeg, null));
        
        doReturn(map).when(appProperties).getProductCategories();

        manager.startConsumers();
        assertEquals(6, manager.consumers.size());
    }

    /**
     * Test spring initialization
     */
    @Test
    public void testPostConstructSpring() {
        assertEquals(2, autoManager.consumers.size());
    }

    /**
     * Test initialization when all consumers
     */
    @Test
    public void testPostConstructAllConsumer() {

        assertEquals(2,
                manager.consumers.get(ProductCategory.AUXILIARY_FILES).size());
        assertEquals("topic", manager.consumers
                .get(ProductCategory.AUXILIARY_FILES).get("topic").getTopic());
        assertEquals(AuxiliaryFileDto.class,
                manager.consumers.get(ProductCategory.AUXILIARY_FILES)
                        .get("topic").getConsumedMsgClass());
        assertEquals("topic-other",
                manager.consumers.get(ProductCategory.AUXILIARY_FILES)
                        .get("topic-other").getTopic());
        assertEquals(AuxiliaryFileDto.class,
                manager.consumers.get(ProductCategory.AUXILIARY_FILES)
                        .get("topic-other").getConsumedMsgClass());

        assertEquals(1,
                manager.consumers.get(ProductCategory.EDRS_SESSIONS).size());
        assertEquals("topic", manager.consumers
                .get(ProductCategory.EDRS_SESSIONS).get("topic").getTopic());
        assertEquals(EdrsSessionDto.class,
                manager.consumers.get(ProductCategory.EDRS_SESSIONS)
                        .get("topic").getConsumedMsgClass());

        assertEquals(1,
                manager.consumers.get(ProductCategory.LEVEL_PRODUCTS).size());
        assertEquals("topic", manager.consumers
                .get(ProductCategory.LEVEL_PRODUCTS).get("topic").getTopic());
        assertEquals(LevelProductDto.class,
                manager.consumers.get(ProductCategory.LEVEL_PRODUCTS)
                        .get("topic").getConsumedMsgClass());

        assertEquals(1,
                manager.consumers.get(ProductCategory.LEVEL_JOBS).size());
        assertEquals("topic", manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").getTopic());
        assertEquals(LevelJobDto.class,
                manager.consumers.get(ProductCategory.LEVEL_JOBS).get("topic")
                        .getConsumedMsgClass());

        assertEquals(2,
                manager.consumers.get(ProductCategory.LEVEL_REPORTS).size());
        assertEquals("topic", manager.consumers
                .get(ProductCategory.LEVEL_REPORTS).get("topic").getTopic());
        assertEquals(LevelReportDto.class,
                manager.consumers.get(ProductCategory.LEVEL_REPORTS)
                        .get("topic").getConsumedMsgClass());
        assertEquals("another-topic",
                manager.consumers.get(ProductCategory.LEVEL_REPORTS)
                        .get("another-topic").getTopic());
        assertEquals(LevelReportDto.class,
                manager.consumers.get(ProductCategory.LEVEL_REPORTS)
                        .get("another-topic").getConsumedMsgClass());

        assertEquals(1,
                manager.consumers.get(ProductCategory.LEVEL_SEGMENTS).size());
        assertEquals("topic", manager.consumers.get(ProductCategory.LEVEL_SEGMENTS)
                .get("topic").getTopic());
        assertEquals(LevelSegmentDto.class,
                manager.consumers.get(ProductCategory.LEVEL_SEGMENTS).get("topic")
                        .getConsumedMsgClass());
        
    }

    /**
     * Test nextMessage when no available consumer
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testNextMessageWhenCategoryNotInit()
            throws AbstractCodedException {
        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.AUXILIARY_FILES)));
        thrown.expect(hasProperty("type", is("consumer")));

        autoManager.nextMessage(ProductCategory.AUXILIARY_FILES);

    }

    /**
     * Test nextMessages for all categories when app server return no messages
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testNextMessageWhenAppServerReturnNull()
            throws AbstractCodedException {
        testNextMessageWhenNoResponse(persistAuxiliaryFilesService,
                ProductCategory.AUXILIARY_FILES);
        testNextMessageWhenNoResponse(persistEdrsSessionsService,
                ProductCategory.EDRS_SESSIONS);
        testNextMessageWhenNoResponse(persistLevelJobsService,
                ProductCategory.LEVEL_JOBS);
        testNextMessageWhenNoResponse(persistLevelProductsService,
                ProductCategory.LEVEL_PRODUCTS);
        testNextMessageWhenNoResponse(persistLevelReportsService,
                ProductCategory.LEVEL_REPORTS);
        testNextMessageWhenNoResponse(persistLevelSegmentsService,
                ProductCategory.LEVEL_SEGMENTS);
    }

    /**
     * Mock and check next message for a given category when server return no
     * messages
     * 
     * @param mockedService
     * @param category
     * @throws AbstractCodedException
     */
    private void testNextMessageWhenNoResponse(
            final GenericAppCatalogMqiService<?> mockedService,
            final ProductCategory category) throws AbstractCodedException {

        doReturn(null, new ArrayList<>()).when(mockedService)
                .next(Mockito.anyString());
        doReturn(category).when(mockedService).getCategory();

        assertNull(manager.nextMessage(category));
        assertNull(manager.nextMessage(category));
        verify(mockedService, times(2)).next(Mockito.eq("pod-name"));
    }

    /**
     * Test nextMessages for all categories when app server return no messages
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testNextMessage() throws AbstractCodedException {
        testNextMessage(persistAuxiliaryFilesService,
                ProductCategory.AUXILIARY_FILES, 1);
        testNextMessage(persistEdrsSessionsService,
                ProductCategory.EDRS_SESSIONS, 2);
        testNextMessage(persistLevelJobsService, ProductCategory.LEVEL_JOBS, 3);
        testNextMessage(persistLevelProductsService,
                ProductCategory.LEVEL_PRODUCTS, 4);
        testNextMessage(persistLevelReportsService,
                ProductCategory.LEVEL_REPORTS, 5);
        testNextMessage(persistLevelSegmentsService,
                ProductCategory.LEVEL_SEGMENTS, 6);
        testNextMessage(persistErrorsService,
                ProductCategory.LEVEL_SEGMENTS, 7);
    }

    /**
     * Mock and check next message for a given category when server return a
     * list of objects: the first is processing by another pod, the second and
     * the thrid are ok
     * 
     * @param mockedService
     * @param category
     * @throws AbstractCodedException
     */
    private void testNextMessage(
            final GenericAppCatalogMqiService<?> mockedService,
            final ProductCategory category, int nbCallOtherService)
            throws AbstractCodedException {
        doReturn(category).when(mockedService).getCategory();

        // Processing by another pod
        MqiGenericMessageDto<?> msg1 =
                new MqiGenericMessageDto<>(category, 1, "topic", 1, 10);
        msg1.setState(MqiStateMessageEnum.SEND);
        msg1.setCreationDate(new Date());
        msg1.setSendingPod("other-pod");
        doReturn(true).when(otherService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.eq(1L));

        // Status read
        MqiGenericMessageDto<?> msg2 =
                new MqiGenericMessageDto<>(category, 2, "topic", 1, 11);
        msg2.setState(MqiStateMessageEnum.READ);
        msg2.setCreationDate(new Date());
        doReturn(true).when(mockedService).send(Mockito.eq(2L), Mockito.any());

        // Status read
        MqiGenericMessageDto<?> msg3 =
                new MqiGenericMessageDto<>(category, 3, "topic", 2, 11);
        msg3.setState(MqiStateMessageEnum.READ);
        msg3.setCreationDate(new Date());
        doReturn(true).when(mockedService).send(Mockito.eq(3L), Mockito.any());

        doReturn(Arrays.asList(msg1, msg2, msg3)).when(mockedService)
                .next(Mockito.anyString());

        assertEquals(new GenericMessageDto<>(2, "topic", null),
                manager.nextMessage(category));
        verify(mockedService, times(1)).next(Mockito.eq("pod-name"));
        verify(mockedService, times(1)).send(Mockito.eq(2L), Mockito.any());
        verify(otherService, times(1)).isProcessing(Mockito.eq("other-pod"),
                Mockito.eq(category), Mockito.eq(1L));
        verifyNoMoreInteractions(otherService);

    }

    /**
     * Test ackMessage when no available consumer
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testAckMessageWhenCategoryNotInit()
            throws AbstractCodedException {
        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.AUXILIARY_FILES)));
        thrown.expect(hasProperty("type", is("consumer")));

        autoManager.ackMessage(ProductCategory.AUXILIARY_FILES, 123, Ack.OK,
                false);

    }

    @Test
    public void testAckWhenStopNotAsk()
            throws AbstractCodedException, InterruptedException {

        LevelJobDto dto = new LevelJobDto(ProductFamily.L1_JOB, "product-name", "NRT",
                "work-dir", "job-order");
        MqiGenericMessageDto<LevelJobDto> message =
                new MqiGenericMessageDto<LevelJobDto>(
                        ProductCategory.LEVEL_JOBS, 123, "topic", 1, 22, dto);

        doReturn(true).when(persistLevelJobsService).ack(Mockito.eq(123L),
                Mockito.any());
        doReturn(message).when(persistLevelJobsService).get(Mockito.eq(123L));
        doReturn(0).when(persistLevelJobsService)
                .getNbReadingMessages(Mockito.anyString(), Mockito.anyString());

        ResumeDetails expectedRd = new ResumeDetails("topic", dto);

        Thread.sleep(2000);
        manager.consumers.get(ProductCategory.LEVEL_JOBS).get("topic").pause();

        Thread.sleep(3000);
        assertTrue(manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").isPaused());
        ResumeDetails rd = manager.ackMessage(ProductCategory.LEVEL_JOBS, 123,
                Ack.OK, false);
        assertEquals(expectedRd, rd);

        // Check resume call
        Thread.sleep(2000);
        assertFalse(manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").isPaused());
    }

    @Test
    public void testAckWhenStopNotAskButTopicUnknown()
            throws AbstractCodedException, InterruptedException {

        LevelJobDto dto = new LevelJobDto(ProductFamily.L1_JOB, "product-name", "NRT",
                "work-dir", "job-order");
        MqiGenericMessageDto<LevelJobDto> message =
                new MqiGenericMessageDto<LevelJobDto>(
                        ProductCategory.LEVEL_JOBS, 123, "topic-unknown", 1, 22,
                        dto);

        doReturn(true).when(persistLevelJobsService).ack(Mockito.eq(123L),
                Mockito.any());
        doReturn(message).when(persistLevelJobsService).get(Mockito.eq(123L));
        doReturn(0).when(persistLevelJobsService)
                .getNbReadingMessages(Mockito.anyString(), Mockito.anyString());

        ResumeDetails expectedRd = new ResumeDetails("topic-unknown", dto);

        Thread.sleep(2000);
        manager.consumers.get(ProductCategory.LEVEL_JOBS).get("topic").pause();

        Thread.sleep(2500);
        assertTrue(manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").isPaused());
        ResumeDetails rd = manager.ackMessage(ProductCategory.LEVEL_JOBS, 123,
                Ack.OK, false);
        assertEquals(expectedRd, rd);

        // Check resume call
        Thread.sleep(2000);
        assertTrue(manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").isPaused());

        manager.consumers.get(ProductCategory.LEVEL_JOBS).get("topic").resume();
    }

    @Test
    public void testAckWhenStopAsk()
            throws AbstractCodedException, InterruptedException {

        LevelJobDto dto = new LevelJobDto(ProductFamily.L1_JOB, "product-name", "NRT",
                "work-dir", "job-order");
        MqiGenericMessageDto<LevelJobDto> message =
                new MqiGenericMessageDto<LevelJobDto>(
                        ProductCategory.LEVEL_JOBS, 123, "topic4", 1, 22, dto);

        doReturn(true).when(persistLevelJobsService).ack(Mockito.eq(123L),
                Mockito.any());
        doReturn(message).when(persistLevelJobsService).get(Mockito.eq(123L));
        doReturn(0).when(persistLevelJobsService)
                .getNbReadingMessages(Mockito.anyString(), Mockito.anyString());

        ResumeDetails expectedRd = new ResumeDetails("topic4", dto);

        Thread.sleep(2000);
        manager.consumers.get(ProductCategory.LEVEL_JOBS).get("topic").pause();

        Thread.sleep(2000);
        assertTrue(manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").isPaused());
        ResumeDetails rd = manager.ackMessage(ProductCategory.LEVEL_JOBS, 123,
                Ack.OK, true);
        assertEquals(expectedRd, rd);

        // Check resume call
        Thread.sleep(2000);
        assertTrue(manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").isPaused());
    }

    
    @Test
    public void testAckWhenStopAskL2()
            throws AbstractCodedException, InterruptedException {

        LevelJobDto dto = new LevelJobDto(ProductFamily.L2_JOB, "product-name", "NRT",
                "work-dir", "job-order");
        MqiGenericMessageDto<LevelJobDto> message =
                new MqiGenericMessageDto<LevelJobDto>(
                        ProductCategory.LEVEL_JOBS, 123, "topic5", 1, 22, dto);

        doReturn(true).when(persistLevelJobsService).ack(Mockito.eq(123L),
                Mockito.any());
        doReturn(message).when(persistLevelJobsService).get(Mockito.eq(123L));
        doReturn(0).when(persistLevelJobsService)
                .getNbReadingMessages(Mockito.anyString(), Mockito.anyString());

        ResumeDetails expectedRd = new ResumeDetails("topic5", dto);

        Thread.sleep(2000);
        manager.consumers.get(ProductCategory.LEVEL_JOBS).get("topic").pause();

        Thread.sleep(2000);
        assertTrue(manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").isPaused());
        ResumeDetails rd = manager.ackMessage(ProductCategory.LEVEL_JOBS, 123,
                Ack.OK, true);
        assertEquals(expectedRd, rd);

        // Check resume call
        Thread.sleep(2000);
        assertTrue(manager.consumers.get(ProductCategory.LEVEL_JOBS)
                .get("topic").isPaused());
    }
    
    /**
     * Test send when message is READ
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendWhenMessageRead() throws AbstractCodedException {
        testSendMessageWhenAskOtherAppNotNeeded(MqiStateMessageEnum.READ);
    }

    /**
     * Test send when message is processing by same pod
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendWhenMessageProcessingBySamePod()
            throws AbstractCodedException {
        testSendMessageWhenAskOtherAppNotNeeded(MqiStateMessageEnum.SEND);
    }

    /**
     * Internal check when asking for other app is not needed
     * 
     * @param state
     * @throws AbstractCodedException
     */
    private void testSendMessageWhenAskOtherAppNotNeeded(
            MqiStateMessageEnum state) throws AbstractCodedException {

        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(state);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("pod-name");

        MqiLightMessageDto msgLight2 = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1235, "topic", 1, 111);
        msgLight2.setState(state);
        msgLight2.setReadingPod("pod-name");
        msgLight2.setSendingPod("pod-name");

        doReturn(true).when(otherService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());
        doReturn(true).when(persistAuxiliaryFilesService)
                .send(Mockito.eq(1234L), Mockito.any());
        doReturn(false).when(persistAuxiliaryFilesService)
                .send(Mockito.eq(1235L), Mockito.any());

        MqiSendMessageDto expected = new MqiSendMessageDto("pod-name", false);

        assertTrue(manager.send(persistAuxiliaryFilesService, msgLight));
        assertFalse(manager.send(persistAuxiliaryFilesService, msgLight2));
        verifyZeroInteractions(otherService);
        verify(persistAuxiliaryFilesService, times(1)).send(Mockito.eq(1234L),
                Mockito.eq(expected));
        verify(persistAuxiliaryFilesService, times(1)).send(Mockito.eq(1235L),
                Mockito.eq(expected));
        verifyNoMoreInteractions(persistAuxiliaryFilesService);
    }

    /**
     * Test send when message is processing
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendWhenMessageProcessingByAnotherAndResponseTrue()
            throws AbstractCodedException {

        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MqiStateMessageEnum.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        doReturn(true).when(otherService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());
        doReturn(ProductCategory.AUXILIARY_FILES)
                .when(persistAuxiliaryFilesService).getCategory();

        // First time: msgLightForceRead
        assertFalse(manager.send(persistAuxiliaryFilesService, msgLight));
        verify(otherService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
    }

    /**
     * Test messageShallBeIgnored when no response from the other app
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendWhenNoResponse() throws AbstractCodedException {

        doThrow(new StatusProcessingApiError("uri", "error message"))
                .when(otherService).isProcessing(Mockito.anyString(),
                        Mockito.any(), Mockito.anyLong());

        testSendWhenMessageProcessingByAnotherAndResponseFalse();
    }

    /**
     * Test messageShallBeIgnored when the other app return false
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testSendWhenResponseFalse() throws AbstractCodedException {

        doReturn(false).when(otherService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        testSendWhenMessageProcessingByAnotherAndResponseFalse();
    }

    private void testSendWhenMessageProcessingByAnotherAndResponseFalse()
            throws AbstractCodedException {

        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234L, "topic", 1, 111);
        msgLight.setState(MqiStateMessageEnum.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        MqiLightMessageDto msgLight2 = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1235L, "topic", 1, 111);
        msgLight2.setState(MqiStateMessageEnum.SEND);
        msgLight2.setReadingPod("pod-name");
        msgLight2.setSendingPod("other-name");

        doReturn(true).when(persistAuxiliaryFilesService)
                .send(Mockito.eq(1234L), Mockito.any());
        doReturn(false).when(persistAuxiliaryFilesService)
                .send(Mockito.eq(1235L), Mockito.any());
        doReturn(ProductCategory.AUXILIARY_FILES)
                .when(persistAuxiliaryFilesService).getCategory();

        MqiSendMessageDto expected = new MqiSendMessageDto("pod-name", true);

        assertTrue(manager.send(persistAuxiliaryFilesService, msgLight));
        assertFalse(manager.send(persistAuxiliaryFilesService, msgLight2));
        verify(otherService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(otherService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1235L));
        verify(persistAuxiliaryFilesService, times(1)).send(Mockito.eq(1234L),
                Mockito.eq(expected));
        verify(persistAuxiliaryFilesService, times(1)).send(Mockito.eq(1235L),
                Mockito.eq(expected));
    }
}
