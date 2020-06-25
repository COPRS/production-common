package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer;

import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
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
public class TestHandlingOfInvalidQueueElementsIT {

	// dummy class for generating invalid object
	public static class FailClass {
		private int foo;
		private String bar;
		
		public FailClass(final int foo, final String bar) {
			this.foo = foo;
			this.bar = bar;
		}
		
		public int getFoo() {
			return foo;
		}
		
		public void setFoo(final int foo) {
			this.foo = foo;
		}
		
		public String getBar() {
			return bar;
		}

		public void setBar(final String bar) {
			this.bar = bar;
		}
		
		@Override
		public String toString() {
			return "FailClass [foo=" + foo + ", bar=" + bar + "]";
		}	
	}
	
	private static final String SENDER_TOPIC = GenericKafkaUtils.TOPIC_AUXILIARY_FILES;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, SENDER_TOPIC);
	
    @Autowired
    private KafkaProperties properties;

    @Autowired
    private AppStatus appStatus;

    @Mock
    private MessagePersistence<ProductionEvent> messagePersistence;

	private GenericConsumer<ProductionEvent> uut;
	
	@Before
	public void setUp() {
        MockitoAnnotations.initMocks(this);
        
		uut = new GenericConsumer.Factory<>(properties, messagePersistence,appStatus)
				.newConsumerFor(ProductCategory.AUXILIARY_FILES, 100, GenericKafkaUtils.TOPIC_AUXILIARY_FILES);
	
		uut.start();

		// wait until the container has the required number of assigned partitions
		ContainerTestUtils.waitForAssignment(uut.container(), embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());
	}

	@After
	public void tearDown() {
		uut.stop();
	}

	@Test
	public void testNominalConsumption_ShallNotFail() throws Exception {		
        final ProductionEvent expected = new ProductionEvent("1", "2", ProductFamily.AUXILIARY_FILE); 
        send(expected);

		verify(messagePersistence, Mockito.timeout(1000).times(1)).read(Mockito.argThat(equalsRecordWith(expected)), Mockito.any(), Mockito.any(), Mockito.eq(ProductCategory.AUXILIARY_FILES));
	}
	
	@Test
	public void testInvalidTypeConsumption_ShallNotFailAndContinueWithNextElement() throws Exception {		
		// insert string
        send("fooBar");          

        // insert alien class object
        send(new FailClass(42, "hello"));

        // insert valid object        
        final ProductionEvent expected = new ProductionEvent("1", "2", ProductFamily.AUXILIARY_FILE);          
        send(expected);

        // only the production event shall be read at all no other invocations
		verify(messagePersistence, Mockito.timeout(1000).times(1)).read(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		verify(messagePersistence, Mockito.timeout(1000).times(1)).read(Mockito.argThat(equalsRecordWith(expected)), Mockito.any(), Mockito.any(), Mockito.eq(ProductCategory.AUXILIARY_FILES));
	}
	
	private <T> void send(final T mess) throws InterruptedException, ExecutionException {
        final Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        final ProducerFactory<Integer, T> pf = new DefaultKafkaProducerFactory<>(senderProps);
        final KafkaTemplate<Integer, T> template = new KafkaTemplate<>(pf);    
        template.send(SENDER_TOPIC, mess).get();
	}

	private ArgumentMatcher<ConsumerRecord<String, ProductionEvent>> equalsRecordWith(ProductionEvent event) {
		return new ArgumentMatcher<ConsumerRecord<String, ProductionEvent>>() {
			@Override
			public boolean matches(ConsumerRecord<String, ProductionEvent> record) {
				return record.value().equals(event);
			}

			@Override
			public String toString() {
				return String.format("consumer record with value %s", event);
			}
		};
	}
}
