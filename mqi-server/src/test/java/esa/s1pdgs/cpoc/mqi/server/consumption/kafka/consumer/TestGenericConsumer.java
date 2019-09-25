package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.logging.log4j.core.appender.mom.kafka.DefaultKafkaProducerFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatus;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestGenericConsumer {	
	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, false, GenericKafkaUtils.TOPIC_AUXILIARY_FILES);
    
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
    private AppCatalogMqiService service;

    @Mock
    private OtherApplicationService otherService;
    
    @Test
    public void testSpringKafka() throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("sampleConsumer", "false", embeddedKafka);
        consumerProps.put("auto.offset.reset", "earliest");
        DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProps = new ContainerProperties("messages");

        final CountDownLatch latch = new CountDownLatch(4);
        containerProps.setMessageListener((AcknowledgingMessageListener<Integer, String>) (message, ack) -> {
            LOGGER.info("Receiving: " + message);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            latch.countDown();
        });
        KafkaMessageListenerContainer<Integer, String> container =
                new KafkaMessageListenerContainer<>(cf, containerProps);
        container.setBeanName("sampleConsumer");


        container.start();
//        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());

        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka);
        ProducerFactory<Integer, String> pf = new DefaultKafkaProducerFactory<Integer, String>(senderProps);
        KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("messages");
        template.sendDefault(0, 0, "message1");
        template.sendDefault(0, 1, "message2");
        template.sendDefault(1, 2, "message3");
        template.sendDefault(1, 3, "message4");
        template.flush();
        assertTrue(latch.await(20, TimeUnit.SECONDS));
        container.stop();
    }
    
    
    @Test
    public void testConsumer_OnInvalidElement_ShallConsumeDumpAndContinue() throws Exception {    
//    	System.out.println("Kafka props: " + properties);
//		final GenericConsumer<ProductDto> uut = new GenericConsumer<>(
//				  ProductCategory.AUXILIARY_FILES, 
//				  properties, 
//				  service, 
//				  otherService,
//				  appStatus, 
//				  GenericKafkaUtils.TOPIC_AUXILIARY_FILES,
//				  100, 
//				  ProductDto.class
//		);
//
//		embeddedKafka.getEmbeddedKafka().
//		
//		System.out.println("start");
//		uut.start();
//		System.out.println("sleep");
//		Thread.sleep(2000);
//
//
//		verify(service, never())
//			.read(Mockito.any(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(), Mockito.any());

  //kafkaUtils.sendMessageToKafka("Totally invalid entry", GenericKafkaUtils.TOPIC_AUXILIARY_FILES);
          
          
          
    }
	

}
