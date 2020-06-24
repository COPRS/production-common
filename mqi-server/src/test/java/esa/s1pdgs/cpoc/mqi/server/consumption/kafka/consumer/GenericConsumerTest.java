package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePersistence;
import esa.s1pdgs.cpoc.mqi.server.service.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.test.DataUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("noconsumer")
public class GenericConsumerTest {

    private static final String CONSUMER_TOPIC = "consumerTopic";

    /**
     * Properties to test
     */
    @Autowired
    private KafkaProperties properties;

    /**
     * Application status
     */
    @Autowired
    private AppStatus appStatus;

    @Mock
    private MessagePersistence messagePersistence;

    @Mock
    private OtherApplicationService otherService;

    private AppCatMessageDto<ProductionEvent> messageLight1 = DataUtils.getLightMessage1();

    private AppCatMessageDto<ProductionEvent> messageLight2 = DataUtils.getLightMessage2();

    private AppCatMessageDto<ProductionEvent> messageLight3 = DataUtils.getLightMessage1();

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, false, CONSUMER_TOPIC, GenericKafkaUtils.TOPIC_AUXILIARY_FILES);

    private GenericConsumer<ProductionEvent> uut; 
    
    
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        properties.setHostname("test-host");

        messageLight2.setState(MessageState.SEND);
        messageLight2.setSendingPod("test-host");
        messageLight3.setState(MessageState.SEND);
        messageLight3.setSendingPod("other-host");

        uut = new GenericConsumer.Factory(properties, messagePersistence, otherService, appStatus)
        		.newConsumerFor(ProductCategory.AUXILIARY_FILES, 100, GenericKafkaUtils.TOPIC_AUXILIARY_FILES);
    }

    @Test
    public void testConstructor() {
        assertEquals(GenericKafkaUtils.TOPIC_AUXILIARY_FILES, uut.getTopic());
        assertEquals(ProductionEvent.class, uut.getConsumedMsgClass());
    }

    @Test
    public void testAuxiliaryFilesConsumer() throws Exception {
        final ProductionEvent dto = new ProductionEvent("product-name", "key-obs", ProductFamily.AUXILIARY_FILE);
        final ProductionEvent dto2 = new ProductionEvent("product-name-2", "key-obs-2", ProductFamily.AUXILIARY_FILE);
        final GenericKafkaUtils<ProductionEvent> kafkaUtils = new GenericKafkaUtils<>(embeddedKafka);

        uut.start();
        Thread.sleep(5000);
        verify(messagePersistence, never()).read(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Send first DTO
        kafkaUtils.sendMessageToKafka(dto,
                GenericKafkaUtils.TOPIC_AUXILIARY_FILES);
        Thread.sleep(1500);

        final ConsumerRecord<String, AppCatReadMessageDto<ProductionEvent>> expected =
                new ConsumerRecord("topic",
                        23,
                        343,
                        "key",
                        new AppCatReadMessageDto<ProductionEvent>("wrappers",
                                "test-host", false, dto));

        verify(messagePersistence, times(1)).read(Mockito.eq(expected), Mockito.any(), Mockito.eq(uut), Mockito.eq(ProductCategory.AUXILIARY_FILES));

        // Send second DTO without resuming consumer
        kafkaUtils.sendMessageToKafka(dto2,
                GenericKafkaUtils.TOPIC_AUXILIARY_FILES);
        Thread.sleep(1000);
        verify(messagePersistence, times(1)).read(Mockito.eq(expected), Mockito.any(), Mockito.eq(uut), Mockito.eq(ProductCategory.AUXILIARY_FILES));

        // Resume consumer
        uut.resume();
        Thread.sleep(1000);

        final ConsumerRecord<String, AppCatReadMessageDto<ProductionEvent>> expected2 =
                new ConsumerRecord("topic",
                        23,
                        344,
                        "key",
                        new AppCatReadMessageDto<ProductionEvent>("wrappers",
                                "test-host", false, dto2));

        verify(messagePersistence, times(2)).read(Mockito.any(), Mockito.any(), Mockito.eq(uut), Mockito.eq(ProductCategory.AUXILIARY_FILES));
        verify(messagePersistence, times(1)).read(Mockito.eq(expected2), Mockito.any(), Mockito.eq(uut), Mockito.eq(ProductCategory.AUXILIARY_FILES));

    }
}
