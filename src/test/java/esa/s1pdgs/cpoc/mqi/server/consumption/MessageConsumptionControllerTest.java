package esa.s1pdgs.cpoc.mqi.server.consumption;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryConsumptionProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.MessageConsumptionController;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
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

    @Autowired
    private MessageConsumptionController autoManager;

    private MessageConsumptionController manager;

    private GenericKafkaUtils<EdrsSessionDto> kafkaUtilsEdrsSession;

    private GenericKafkaUtils<LevelJobDto> kafkaUtilsJobs;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        manager = new MessageConsumptionController(appProperties,
                kafkaProperties);

        kafkaUtilsEdrsSession =
                new GenericKafkaUtils<EdrsSessionDto>(embeddedKafka);

        kafkaUtilsJobs = new GenericKafkaUtils<LevelJobDto>(embeddedKafka);
    }

    @Test
    public void testPostConstructSpring() {
        assertEquals(2, autoManager.consumers.size());
    }

    /**
     * Test the nextMessage when consumer OK
     * 
     * @throws MqiCategoryNotAvailable
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNextMessage() throws MqiCategoryNotAvailable,
            InterruptedException, ExecutionException {

        // Check case when no message
        GenericMessageDto<EdrsSessionDto> message =
                (GenericMessageDto<EdrsSessionDto>) autoManager
                        .nextMessage(ProductCategory.EDRS_SESSIONS);
        assertNull(message);

        // Check message retrieved when published
        EdrsSessionDto dto1 = new EdrsSessionDto("obs-key-1", 1,
                EdrsSessionFileType.RAW, "S1", "A");
        kafkaUtilsEdrsSession.sendMessageToKafka(dto1,
                GenericKafkaUtils.TOPIC_EDRS_SESSIONS);
        Thread.sleep(1000);
        message = (GenericMessageDto<EdrsSessionDto>) autoManager
                .nextMessage(ProductCategory.EDRS_SESSIONS);
        assertEquals(dto1, message.getBody());
        assertEquals(GenericKafkaUtils.TOPIC_EDRS_SESSIONS,
                message.getInputKey());

        // Check even if a new message is send the nextMessage is the same
        EdrsSessionDto dto2 = new EdrsSessionDto("obs-key-2", 1,
                EdrsSessionFileType.RAW, "S1", "A");
        kafkaUtilsEdrsSession.sendMessageToKafka(dto2,
                GenericKafkaUtils.TOPIC_EDRS_SESSIONS);
        Thread.sleep(1000);
        GenericMessageDto<EdrsSessionDto> message1 =
                (GenericMessageDto<EdrsSessionDto>) autoManager
                        .nextMessage(ProductCategory.EDRS_SESSIONS);
        assertEquals(message, message1);
    }

    /**
     * Test nextMessage when no available consumer
     * 
     * @throws MqiCategoryNotAvailable
     */
    @Test
    public void testNextMessageWhenCategoryNotInit()
            throws MqiCategoryNotAvailable {
        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.AUXILIARY_FILES)));
        thrown.expect(hasProperty("type", is("consumer")));

        autoManager.nextMessage(ProductCategory.AUXILIARY_FILES);

    }

    /**
     * Test the nextMessage when consumer OK
     * 
     * @throws MqiCategoryNotAvailable
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testAckMessage() throws MqiCategoryNotAvailable,
            InterruptedException, ExecutionException {

        long messageIdentifier =
                Objects.hash(GenericKafkaUtils.TOPIC_L0_JOBS, 1);
        long messageIdentifier2 =
                Objects.hash(GenericKafkaUtils.TOPIC_L0_JOBS, 2);

        // Check case when no message
        assertFalse(autoManager.ackMessage(ProductCategory.LEVEL_JOBS,
                messageIdentifier, Ack.OK));
        assertFalse(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .isPaused());

        // Check case message is the right
        // Wait for message consumption
        LevelJobDto dto1 = new LevelJobDto();
        kafkaUtilsJobs.sendMessageToKafka(dto1,
                GenericKafkaUtils.TOPIC_L0_JOBS);
        Thread.sleep(1000);
        assertTrue(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .isPaused());
        assertNotNull(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .getConsumedMessage());
        // ack message
        assertTrue(autoManager.ackMessage(ProductCategory.LEVEL_JOBS,
                messageIdentifier, Ack.OK));
        assertFalse(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .isPaused());
        assertNull(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .getConsumedMessage());

        // Check case message is not the right
        LevelJobDto dto2 = new LevelJobDto();
        kafkaUtilsJobs.sendMessageToKafka(dto2,
                GenericKafkaUtils.TOPIC_L0_JOBS);
        Thread.sleep(500);
        assertFalse(autoManager.ackMessage(ProductCategory.LEVEL_JOBS,
                messageIdentifier, Ack.OK));
        assertTrue(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .isPaused());
        assertNotNull(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .getConsumedMessage());
        assertTrue(autoManager.ackMessage(ProductCategory.LEVEL_JOBS,
                messageIdentifier2, Ack.ERROR));
        assertFalse(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .isPaused());
        assertNull(autoManager.consumers.get(ProductCategory.LEVEL_JOBS)
                .getConsumedMessage());
    }

    /**
     * Test ackMessage when no available consumer
     * 
     * @throws MqiCategoryNotAvailable
     */
    @Test
    public void testAckMessageWhenCategoryNotInit()
            throws MqiCategoryNotAvailable {
        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.AUXILIARY_FILES)));
        thrown.expect(hasProperty("type", is("consumer")));

        autoManager.ackMessage(ProductCategory.AUXILIARY_FILES, 123, Ack.OK);

    }

    @Test
    public void testPostConstructAllConsumer() {
        Map<ProductCategory, ProductCategoryProperties> map = new HashMap<>();
        map.put(ProductCategory.AUXILIARY_FILES, new ProductCategoryProperties(
                new ProductCategoryConsumptionProperties(true, "topic1"),
                null));
        map.put(ProductCategory.LEVEL_PRODUCTS, new ProductCategoryProperties(
                new ProductCategoryConsumptionProperties(true, "topic3"),
                null));
        map.put(ProductCategory.LEVEL_JOBS, new ProductCategoryProperties(
                new ProductCategoryConsumptionProperties(true, "topic4"),
                null));
        map.put(ProductCategory.EDRS_SESSIONS, new ProductCategoryProperties(
                new ProductCategoryConsumptionProperties(true, "topic2"),
                null));
        map.put(ProductCategory.LEVEL_REPORTS, new ProductCategoryProperties(
                new ProductCategoryConsumptionProperties(true, "topic5"),
                null));
        doReturn(map).when(appProperties).getProductCategories();

        manager.startConsumers();
        assertEquals(5, manager.consumers.size());

        assertEquals("topic1", manager.consumers
                .get(ProductCategory.AUXILIARY_FILES).getTopic());
        assertEquals(AuxiliaryFileDto.class, manager.consumers
                .get(ProductCategory.AUXILIARY_FILES).getConsumedMsgClass());

        assertEquals("topic2", manager.consumers
                .get(ProductCategory.EDRS_SESSIONS).getTopic());
        assertEquals(EdrsSessionDto.class, manager.consumers
                .get(ProductCategory.EDRS_SESSIONS).getConsumedMsgClass());

        assertEquals("topic3", manager.consumers
                .get(ProductCategory.LEVEL_PRODUCTS).getTopic());
        assertEquals(LevelProductDto.class, manager.consumers
                .get(ProductCategory.LEVEL_PRODUCTS).getConsumedMsgClass());

        assertEquals("topic4",
                manager.consumers.get(ProductCategory.LEVEL_JOBS).getTopic());
        assertEquals(LevelJobDto.class, manager.consumers
                .get(ProductCategory.LEVEL_JOBS).getConsumedMsgClass());

        assertEquals("topic5", manager.consumers
                .get(ProductCategory.LEVEL_REPORTS).getTopic());
        assertEquals(LevelReportDto.class, manager.consumers
                .get(ProductCategory.LEVEL_REPORTS).getConsumedMsgClass());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadMessageOnePerOne() throws MqiCategoryNotAvailable,
            InterruptedException, ExecutionException {

        // Publish messages
        EdrsSessionDto dto1 = new EdrsSessionDto("obs-key-1", 1,
                EdrsSessionFileType.RAW, "S1", "A");
        EdrsSessionDto dto2 = new EdrsSessionDto("obs-key-2", 1,
                EdrsSessionFileType.RAW, "S1", "A");
        kafkaUtilsEdrsSession.sendMessageToKafka(dto1,
                GenericKafkaUtils.TOPIC_EDRS_SESSIONS);
        kafkaUtilsEdrsSession.sendMessageToKafka(dto2,
                GenericKafkaUtils.TOPIC_EDRS_SESSIONS);

        // First consumption
        Thread.sleep(1000);
        GenericMessageDto<EdrsSessionDto> message1 =
                (GenericMessageDto<EdrsSessionDto>) autoManager
                        .nextMessage(ProductCategory.EDRS_SESSIONS);
        assertEquals(dto1, message1.getBody());
        assertEquals(GenericKafkaUtils.TOPIC_EDRS_SESSIONS,
                message1.getInputKey());

        // Acknowledgment
        assertTrue(autoManager.ackMessage(ProductCategory.EDRS_SESSIONS,
                message1.getIdentifier(), Ack.OK));

        // Read second message
        Thread.sleep(1000);
        GenericMessageDto<EdrsSessionDto> message2 =
                (GenericMessageDto<EdrsSessionDto>) autoManager
                        .nextMessage(ProductCategory.EDRS_SESSIONS);
        assertNotEquals(message1.getIdentifier(), message2.getIdentifier());
        assertEquals(dto2, message2.getBody());
        assertEquals(GenericKafkaUtils.TOPIC_EDRS_SESSIONS,
                message2.getInputKey());

        // Acknowledgment
        assertTrue(autoManager.ackMessage(ProductCategory.EDRS_SESSIONS,
                message2.getIdentifier(), Ack.OK));

    }
}
