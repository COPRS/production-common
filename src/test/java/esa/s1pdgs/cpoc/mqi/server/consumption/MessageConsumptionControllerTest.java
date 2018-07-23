package esa.s1pdgs.cpoc.mqi.server.consumption;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.Map;
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

import esa.s1pdgs.cpoc.appcatalog.client.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
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

    private GenericKafkaUtils<EdrsSessionDto> kafkaUtilsEdrsSession;

    private GenericKafkaUtils<LevelJobDto> kafkaUtilsJobs;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        manager = new MessageConsumptionController(appProperties,
                kafkaProperties, persistAuxiliaryFilesService,
                persistEdrsSessionsService, persistLevelJobsService,
                persistLevelProductsService, persistLevelReportsService,
                otherService, appStatus);

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
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNextMessage() throws InterruptedException,
            ExecutionException, AbstractCodedException {
        // TODO
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
     * Test the nextMessage when consumer OK
     * 
     * @throws MqiCategoryNotAvailable
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testAckMessage() throws MqiCategoryNotAvailable,
            InterruptedException, ExecutionException {

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

    @Test
    public void testReadMessageOnePerOne() throws MqiCategoryNotAvailable,
            InterruptedException, ExecutionException {

    }
}
