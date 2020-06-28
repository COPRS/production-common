package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePersistence;

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
    private MessagePersistence<ProductionEvent> messagePersistence;

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, false, CONSUMER_TOPIC, GenericKafkaUtils.TOPIC_AUXILIARY_FILES);

    private GenericConsumer<ProductionEvent> uut; 
    
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        properties.setHostname("test-host");

        uut = new GenericConsumer.Factory<>(properties, messagePersistence, appStatus)
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
        verify(messagePersistence, never()).read(any(), any(), any(), any());

        // Send first DTO
        kafkaUtils.sendMessageToKafka(dto,
                GenericKafkaUtils.TOPIC_AUXILIARY_FILES);
        Thread.sleep(1500);

        verify(messagePersistence, times(1)).read(argThat(isConsumerRecordWithDto(dto)), any(), eq(uut), eq(ProductCategory.AUXILIARY_FILES));

        // Send second DTO without resuming consumer
        kafkaUtils.sendMessageToKafka(dto2,
                GenericKafkaUtils.TOPIC_AUXILIARY_FILES);
        Thread.sleep(1000);
        verify(messagePersistence, times(1)).read(argThat(isConsumerRecordWithDto(dto)), any(), eq(uut), eq(ProductCategory.AUXILIARY_FILES));

        // Resume consumer
        uut.resume();
        Thread.sleep(1000);

        verify(messagePersistence, times(2)).read(any(), any(), eq(uut), eq(ProductCategory.AUXILIARY_FILES));
        verify(messagePersistence, times(1)).read(argThat(isConsumerRecordWithDto(dto2)), any(), eq(uut), eq(ProductCategory.AUXILIARY_FILES));

    }

    private ArgumentMatcher<ConsumerRecord<String, ProductionEvent>> isConsumerRecordWithDto(ProductionEvent dto) {
        return new ArgumentMatcher<ConsumerRecord<String, ProductionEvent>>() {
            @Override
            public boolean matches(ConsumerRecord<String, ProductionEvent> actualRecord) {
                return actualRecord.value().equals(dto);
            }

            @Override
            public String toString() {
                return "ConsumerRecord with dto " + dto;
            }
        };
    }
}
