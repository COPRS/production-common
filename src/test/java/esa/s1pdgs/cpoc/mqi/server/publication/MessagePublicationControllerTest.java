package esa.s1pdgs.cpoc.mqi.server.publication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
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
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiRouteNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryPublicationProperties;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.converter.XmlConverter;
import esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer.ErrorsProducer;

/**
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MessagePublicationControllerTest {

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true,
            GenericKafkaUtils.TOPIC_ERROR, GenericKafkaUtils.TOPIC_L0_PRODUCTS,
            GenericKafkaUtils.TOPIC_L0_ACNS, GenericKafkaUtils.TOPIC_L0_REPORTS,
            GenericKafkaUtils.TOPIC_L1_PRODUCTS,
            GenericKafkaUtils.TOPIC_AUXILIARY_FILES,
            GenericKafkaUtils.TOPIC_L1_ACNS, GenericKafkaUtils.TOPIC_L1_REPORTS,
            GenericKafkaUtils.TOPIC_L0_JOBS, GenericKafkaUtils.TOPIC_L1_JOBS,
            GenericKafkaUtils.TOPIC_EDRS_SESSIONS,
            GenericKafkaUtils.TOPIC_L0_SEGMENTS);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private XmlConverter xmlConverter;

    @Autowired
    private ErrorsProducer errorsProducer;

    @Autowired
    private MessagePublicationController autowiredController;

    @Mock
    private ApplicationProperties appProperties;

    private MessagePublicationController customController;

    private GenericKafkaUtils<LevelProductDto> kafkaUtilsProducts;

    private GenericKafkaUtils<LevelReportDto> kafkaUtilsReports;

    private GenericKafkaUtils<EdrsSessionDto> kafkaUtilsEdrsSession;

    private GenericKafkaUtils<LevelJobDto> kafkaUtilsJobs;

    private GenericKafkaUtils<AuxiliaryFileDto> kafkaUtilsAux;

    private GenericKafkaUtils<LevelSegmentDto> kafkaUtilsSegments;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        kafkaUtilsProducts =
                new GenericKafkaUtils<LevelProductDto>(embeddedKafka);

        kafkaUtilsReports =
                new GenericKafkaUtils<LevelReportDto>(embeddedKafka);

        kafkaUtilsEdrsSession =
                new GenericKafkaUtils<EdrsSessionDto>(embeddedKafka);

        kafkaUtilsJobs = new GenericKafkaUtils<LevelJobDto>(embeddedKafka);

        kafkaUtilsAux = new GenericKafkaUtils<AuxiliaryFileDto>(embeddedKafka);

        kafkaUtilsSegments = new GenericKafkaUtils<LevelSegmentDto>(embeddedKafka);

    }

    private void initCustomControllerForNoPublication() {

        doReturn(new HashMap<>()).when(appProperties).getProductCategories();

        customController = new MessagePublicationController(appProperties,
                kafkaProperties, xmlConverter, errorsProducer);
        try {
            customController.initialize();
        } catch (IOException | JAXBException e) {
            fail(e.getMessage());
        }
    }

    private void initCustomControllerForAllPublication() {
        Map<ProductCategory, ProductCategoryProperties> map = new HashMap<>();
        map.put(ProductCategory.AUXILIARY_FILES, new ProductCategoryProperties(
                null, new ProductCategoryPublicationProperties(true,
                        "./src/test/resources/routing-files/auxiliary-files.xml")));
        map.put(ProductCategory.EDRS_SESSIONS, new ProductCategoryProperties(
                null, new ProductCategoryPublicationProperties(true,
                        "./src/test/resources/routing-files/edrs-sessions.xml")));
        map.put(ProductCategory.LEVEL_JOBS, new ProductCategoryProperties(null,
                new ProductCategoryPublicationProperties(true,
                        "./src/test/resources/routing-files/level-jobs.xml")));
        map.put(ProductCategory.LEVEL_PRODUCTS, new ProductCategoryProperties(
                null, new ProductCategoryPublicationProperties(true,
                        "./src/test/resources/routing-files/level-products.xml")));
        map.put(ProductCategory.LEVEL_REPORTS, new ProductCategoryProperties(
                null, new ProductCategoryPublicationProperties(true,
                        "./src/test/resources/routing-files/level-reports.xml")));
        map.put(ProductCategory.LEVEL_SEGMENTS, new ProductCategoryProperties(
                null, new ProductCategoryPublicationProperties(true,
                        "./src/test/resources/routing-files/level-segments.xml")));
        doReturn(map).when(appProperties).getProductCategories();

        customController = new MessagePublicationController(appProperties,
                kafkaProperties, xmlConverter, errorsProducer);
        try {
            customController.initialize();
        } catch (IOException | JAXBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPostConstruct() {
        assertEquals(2, autowiredController.producers.size());
        assertEquals(2, autowiredController.routing.size());
        assertEquals(4, autowiredController.routing
                .get(ProductCategory.LEVEL_PRODUCTS).getDefaultRoutes().size());
        assertNotNull(
                autowiredController.routing.get(ProductCategory.LEVEL_PRODUCTS)
                        .getDefaultRoute(ProductFamily.L1_ACN));
        assertNotNull(
                autowiredController.routing.get(ProductCategory.LEVEL_PRODUCTS)
                        .getDefaultRoute(ProductFamily.L0_SLICE));
        assertEquals(2, autowiredController.routing
                .get(ProductCategory.LEVEL_REPORTS).getDefaultRoutes().size());
        assertNotNull(
                autowiredController.routing.get(ProductCategory.LEVEL_REPORTS)
                        .getDefaultRoute(ProductFamily.L0_REPORT));
    }

    @Test
    public void testPublishError() throws Exception {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                UUID.randomUUID().toString(), "true", embeddedKafka);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        Consumer<String, String> consumer =
                new DefaultKafkaConsumerFactory<String, String>(props)
                        .createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer,
                GenericKafkaUtils.TOPIC_ERROR);

        autowiredController.publishError("Eroro publish in topic");

        ConsumerRecord<String, String> record = KafkaTestUtils
                .getSingleRecord(consumer, GenericKafkaUtils.TOPIC_ERROR);
        assertEquals("Eroro publish in topic", record.value());
    }

    @Test
    public void testGetTopicWhenNoCategory()
            throws MqiCategoryNotAvailable, MqiRouteNotAvailable {
        initCustomControllerForNoPublication();

        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.EDRS_SESSIONS)));
        thrown.expect(hasProperty("type", is("publisher")));

        customController.getTopic(ProductCategory.EDRS_SESSIONS,
                ProductFamily.EDRS_SESSION);
    }

    @Test
    public void testGetTopicWhenNoRouting()
            throws MqiCategoryNotAvailable, MqiRouteNotAvailable {
        initCustomControllerForAllPublication();
        thrown.expect(MqiRouteNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.EDRS_SESSIONS)));
        thrown.expect(hasProperty("family", is(ProductFamily.AUXILIARY_FILE)));

        customController.getTopic(ProductCategory.EDRS_SESSIONS,
                ProductFamily.AUXILIARY_FILE);
    }

    @Test
    public void publishNoCat() throws MqiPublicationError,
            MqiCategoryNotAvailable, MqiRouteNotAvailable {
        EdrsSessionDto dto = new EdrsSessionDto("obs-key", 1,
                EdrsSessionFileType.RAW, "S1", "A");
        initCustomControllerForNoPublication();

        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.EDRS_SESSIONS)));
        thrown.expect(hasProperty("type", is("publisher")));

        customController.publish(ProductCategory.EDRS_SESSIONS, dto);
    }

    @Test
    public void publishEdrsSessions() throws Exception {
        EdrsSessionDto dto = new EdrsSessionDto("obs-key", 2,
                EdrsSessionFileType.RAW, "S1", "A");
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.EDRS_SESSIONS, dto);

        ConsumerRecord<String, EdrsSessionDto> record =
                kafkaUtilsEdrsSession.getReceivedRecordEdrsSession(
                        GenericKafkaUtils.TOPIC_EDRS_SESSIONS);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishEdrsSessionsNoCat() throws MqiPublicationError,
            MqiCategoryNotAvailable, MqiRouteNotAvailable {
        EdrsSessionDto dto = new EdrsSessionDto("obs-key", 1,
                EdrsSessionFileType.RAW, "S1", "A");
        initCustomControllerForNoPublication();

        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.EDRS_SESSIONS)));
        thrown.expect(hasProperty("type", is("publisher")));

        customController.publishEdrsSessions(dto);
    }

    @Test
    public void publishAuxiliaryFiles() throws Exception {
        AuxiliaryFileDto dto = new AuxiliaryFileDto("product-name", "key-obs");
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.AUXILIARY_FILES, dto);

        ConsumerRecord<String, AuxiliaryFileDto> record = kafkaUtilsAux
                .getReceivedRecordAux(GenericKafkaUtils.TOPIC_AUXILIARY_FILES);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishAuxiliaryFilesNoCat() throws MqiPublicationError,
            MqiCategoryNotAvailable, MqiRouteNotAvailable {
        AuxiliaryFileDto dto = new AuxiliaryFileDto("product-name", "key-obs");
        initCustomControllerForNoPublication();

        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.AUXILIARY_FILES)));
        thrown.expect(hasProperty("type", is("publisher")));

        customController.publishAuxiliaryFiles(dto);
    }

    @Test
    public void publishLevelProducts() throws Exception {
        LevelProductDto dto = new LevelProductDto("product-name", "key-obs",
                ProductFamily.L0_SLICE, "NRT");
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.LEVEL_PRODUCTS, dto);

        ConsumerRecord<String, LevelProductDto> record = kafkaUtilsProducts
                .getReceivedRecordProducts(GenericKafkaUtils.TOPIC_L0_PRODUCTS);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishLevelProducts1() throws Exception {
        LevelProductDto dto = new LevelProductDto("product-name", "key-obs",
                ProductFamily.L1_ACN, "NRT");
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.LEVEL_PRODUCTS, dto);

        ConsumerRecord<String, LevelProductDto> record = kafkaUtilsProducts
                .getReceivedRecordProducts(GenericKafkaUtils.TOPIC_L1_ACNS);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishLevelProductsNoCat() throws MqiPublicationError,
            MqiCategoryNotAvailable, MqiRouteNotAvailable {
        LevelProductDto dto = new LevelProductDto("product-name", "key-obs",
                ProductFamily.L0_SLICE, "NRT");
        initCustomControllerForNoPublication();

        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(hasProperty("type", is("publisher")));

        customController.publishLevelProducts(dto);
    }

    @Test
    public void publishLevelSegments() throws Exception {
        LevelSegmentDto dto = new LevelSegmentDto("product-name", "key-obs",
                ProductFamily.L0_SEGMENT, "FAST");
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.LEVEL_SEGMENTS, dto);

        ConsumerRecord<String, LevelSegmentDto> record = kafkaUtilsSegments
                .getReceivedRecordSegments(GenericKafkaUtils.TOPIC_L0_SEGMENTS);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishLevelSegmentsNoCat() throws MqiPublicationError,
            MqiCategoryNotAvailable, MqiRouteNotAvailable {
        LevelSegmentDto dto = new LevelSegmentDto("product-name", "key-obs",
                ProductFamily.L0_SEGMENT, "FAST");
        initCustomControllerForNoPublication();

        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_SEGMENTS)));
        thrown.expect(hasProperty("type", is("publisher")));

        customController.publishLevelSegments(dto);
    }

    @Test
    public void publishLevelJobs() throws Exception {
        LevelJobDto dto = new LevelJobDto(ProductFamily.L1_JOB, "product-name", "NRT",
                "work-directory", "job-order");
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.LEVEL_JOBS, dto);

        ConsumerRecord<String, LevelJobDto> record = kafkaUtilsJobs
                .getReceivedRecordJobs(GenericKafkaUtils.TOPIC_L1_JOBS);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishLevelJobs1() throws Exception {
        LevelJobDto dto = new LevelJobDto(ProductFamily.L0_JOB, "product-name", "NRT",
                "work-directory", "job-order");
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.LEVEL_JOBS, dto);

        ConsumerRecord<String, LevelJobDto> record = kafkaUtilsJobs
                .getReceivedRecordJobs(GenericKafkaUtils.TOPIC_L0_JOBS);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishLevelJobsNoCat() throws MqiPublicationError,
            MqiCategoryNotAvailable, MqiRouteNotAvailable {
        LevelJobDto dto = new LevelJobDto(ProductFamily.L1_JOB, "product-name", "NRT",
                "work-directory", "job-order");
        initCustomControllerForNoPublication();

        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(hasProperty("category", is(ProductCategory.LEVEL_JOBS)));
        thrown.expect(hasProperty("type", is("publisher")));

        customController.publishLevelJobs(dto);
    }

    @Test
    public void publishLevelReports() throws Exception {
        LevelReportDto dto = new LevelReportDto("product-name", "content",
                ProductFamily.L1_REPORT);
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.LEVEL_REPORTS, dto);

        ConsumerRecord<String, LevelReportDto> record = kafkaUtilsReports
                .getReceivedRecordReports(GenericKafkaUtils.TOPIC_L1_REPORTS);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishLevelReports1() throws Exception {
        LevelReportDto dto = new LevelReportDto("product-name2", "content2",
                ProductFamily.L0_REPORT);
        initCustomControllerForAllPublication();

        customController.publish(ProductCategory.LEVEL_REPORTS, dto);

        ConsumerRecord<String, LevelReportDto> record = kafkaUtilsReports
                .getReceivedRecordReports(GenericKafkaUtils.TOPIC_L0_REPORTS);

        assertEquals(dto, record.value());
    }

    @Test
    public void publishLevelReportsNoCat() throws MqiPublicationError,
            MqiCategoryNotAvailable, MqiRouteNotAvailable {
        LevelReportDto dto = new LevelReportDto("product-name", "content",
                ProductFamily.L0_REPORT);
        initCustomControllerForNoPublication();

        thrown.expect(MqiCategoryNotAvailable.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_REPORTS)));
        thrown.expect(hasProperty("type", is("publisher")));

        customController.publishLevelReports(dto);
    }

}
