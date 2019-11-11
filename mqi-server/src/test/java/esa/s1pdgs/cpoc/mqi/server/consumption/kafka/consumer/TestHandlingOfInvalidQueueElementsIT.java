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
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.server.test.DataUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TestHandlingOfInvalidQueueElementsIT {    
	// dummy class for generating invalid object
	public static class FailClass {
		private int foo;
		private String bar;
		
		public FailClass(int foo, String bar) {
			this.foo = foo;
			this.bar = bar;
		}
		
		public int getFoo() {
			return foo;
		}
		
		public void setFoo(int foo) {
			this.foo = foo;
		}
		
		public String getBar() {
			return bar;
		}

		public void setBar(String bar) {
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
	
//	private KafkaMessageListenerContainer<String, ProductDto> container;
//	private BlockingQueue<ConsumerRecord<String, ProductDto>> records;

    @Autowired
    private KafkaProperties properties;

    @Autowired
    private AppStatusImpl appStatus;

    @Mock
    private AppCatalogMqiService service;

    @Mock
    private OtherApplicationService otherService;
	
	private GenericConsumer<ProductDto> uut;
	
	private BlockingQueue<ProductDto> elements;
	
	@Before
	public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        final AppCatMessageDto<ProductDto> m1 = DataUtils.getLightMessage1();
        final AppCatMessageDto<ProductDto> m2 = DataUtils.getLightMessage2();
        m2.setState(MessageState.SEND);
        m2.setSendingPod("test-host");
        final AppCatMessageDto<ProductDto> m3 = DataUtils.getLightMessage1();
        m3.setState(MessageState.SEND);
        m3.setSendingPod("other-host");

        doReturn(m1, m2, m3).when(service)
                .read(Mockito.any() ,Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(), Mockito.any());
        
        elements = new LinkedBlockingQueue<>();
        
        final MessageConsumer<ProductDto> messageConsumer = new MessageConsumer<ProductDto>() {		
			@Override
			public void consume(ProductDto message) throws Exception {
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
        final ProductDto expected = new ProductDto("1", "2", ProductFamily.AUXILIARY_FILE);          
        send(expected);                
        final ProductDto actual = elements.poll(10, TimeUnit.SECONDS);        
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
        final ProductDto expected = new ProductDto("1", "2", ProductFamily.AUXILIARY_FILE);          
        send(expected);                
        final ProductDto actual = elements.poll(10, TimeUnit.SECONDS);        
        assertEquals(expected, actual);
	}
	
	private final <T> void send(T mess) throws InterruptedException, ExecutionException {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        ProducerFactory<Integer, T> pf = new DefaultKafkaProducerFactory<>(senderProps);
        KafkaTemplate<Integer, T> template = new KafkaTemplate<>(pf);    
        template.send(SENDER_TOPIC, mess).get();
	}
}
