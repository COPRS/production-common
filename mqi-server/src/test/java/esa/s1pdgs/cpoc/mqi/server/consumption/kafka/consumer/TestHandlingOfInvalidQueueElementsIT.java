package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.test.DataUtils;

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
	
	private static String SENDER_TOPIC = GenericKafkaUtils.TOPIC_AUXILIARY_FILES;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, SENDER_TOPIC);
	
//	private KafkaMessageListenerContainer<String, ProductionEvent> container;
//	private BlockingQueue<ConsumerRecord<String, ProductionEvent>> records;

    @Autowired
    private KafkaProperties properties;

    @Autowired
    private AppStatus appStatus;

    @Mock
    private AppCatalogMqiService service;

    @Mock
    private OtherApplicationService otherService;
	
	private GenericConsumer<ProductionEvent> uut;
	
	private BlockingQueue<ProductionEvent> elements;
	
	@Before
	public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        final AppCatMessageDto<ProductionEvent> m1 = DataUtils.getLightMessage1();
        final AppCatMessageDto<ProductionEvent> m2 = DataUtils.getLightMessage2();
        m2.setState(MessageState.SEND);
        m2.setSendingPod("test-host");
        final AppCatMessageDto<ProductionEvent> m3 = DataUtils.getLightMessage1();
        m3.setState(MessageState.SEND);
        m3.setSendingPod("other-host");

        doReturn(m1, m2, m3).when(service)
                .read(Mockito.any() ,Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(), Mockito.any());
        
        elements = new LinkedBlockingQueue<>();
        
        final MessageConsumer<ProductionEvent> messageConsumer = new MessageConsumer<ProductionEvent>() {		
			@Override
			public void consume(final ProductionEvent message) throws Exception {
				elements.add(message);		
			}
		};
        
		uut = new GenericConsumer.Factory(properties,service,otherService,appStatus)
				.newConsumerFor(ProductCategory.AUXILIARY_FILES, 100, GenericKafkaUtils.TOPIC_AUXILIARY_FILES, messageConsumer);
	
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
        final ProductionEvent actual = elements.poll(10, TimeUnit.SECONDS);         
        // make equals work
        expected.setCreationDate(actual.getCreationDate());
        assertEquals(expected, actual);
	}
	
	@Test
	public void testInvalidTypeConsumption_ShallNotFailAndContinueWithNextElement() throws Exception {		
		// insert string
        send("fooBar");          
        elements.poll(10, TimeUnit.SECONDS); 
        
        // insert alien class object
        send(new FailClass(42, "hello"));
        elements.poll(10, TimeUnit.SECONDS); 
        
        // insert valid object        
        final ProductionEvent expected = new ProductionEvent("1", "2", ProductFamily.AUXILIARY_FILE);          
        send(expected);                
        final ProductionEvent actual = elements.poll(10, TimeUnit.SECONDS);    
        // make equals work
        expected.setCreationDate(actual.getCreationDate());
        assertEquals(expected, actual);
	}
	
	private final <T> void send(final T mess) throws InterruptedException, ExecutionException {
        final Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        final ProducerFactory<Integer, T> pf = new DefaultKafkaProducerFactory<>(senderProps);
        final KafkaTemplate<Integer, T> template = new KafkaTemplate<>(pf);    
        template.send(SENDER_TOPIC, mess).get();
	}
}
