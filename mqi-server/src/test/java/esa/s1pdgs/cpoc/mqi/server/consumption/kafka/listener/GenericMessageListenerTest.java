package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import static org.mockito.Mockito.*;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiReadApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties.KafkaConsumerProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePersistence;

/**
 * Test the listener GenericMessageListener
 * 
 * @author Viveris Technologies
 */
public class GenericMessageListenerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Properties
     */
    @Mock
    private KafkaProperties properties;
    @Mock
    private KafkaConsumerProperties consumerProperties;

    /**
     * Service for persisting data
     */
    @Mock
    private MessagePersistence<ProductionEvent> service;

    /**
     * Generic consumer
     */
    @Mock
    private GenericConsumer<ProductionEvent> genericConsumer;

    /**
     * Kafka acknowledgement
     */
    @Mock
    private Acknowledgment acknowledgment;

    /**
     * Application status
     */
    @Mock
    private AppStatus appStatus;

    /**
     * Consumer of message listener
     */
    @Mock
    private Consumer<String, String> onMsgConsumer;

    /**
     * Listener to test
     */
    private GenericMessageListener<ProductionEvent> listener;

    /**
     * Received record
     */
    private ConsumerRecord<String, ProductionEvent> data;

    /**
     * Initialization
     * 
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
        
        final ProductionEvent dto = new ProductionEvent("foo", "bar", ProductFamily.AUXILIARY_FILE);

        data = new ConsumerRecord<>("topic", 1, 145L,
                "key-record", dto);

        doReturn("pod-name").when(properties).getHostname();
        doReturn(consumerProperties).when(properties).getConsumer();
        doReturn("group-name").when(consumerProperties).getGroupId();

        doNothing().when(appStatus).setWaiting();
        doNothing().when(appStatus).setError("MQI");

        doNothing().when(genericConsumer).pause();
        doNothing().when(acknowledgment).acknowledge();

        doReturn(-2L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(0), Mockito.anyString());
        doReturn(-1L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(1), Mockito.anyString());
        doReturn(0L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(2), Mockito.anyString());
        doReturn(128L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(3), Mockito.anyString());
        doThrow(new AppCatalogMqiGetOffsetApiError("uri", "message"))
                        .when(service).getEarliestOffset(Mockito.anyString(),
                                Mockito.eq(4), Mockito.anyString());

        listener = new GenericMessageListener<>(ProductCategory.AUXILIARY_FILES, service, genericConsumer, appStatus);
    }

    /**
     * Test acknowledge
     */
    @Test
    public void testAcknowledge() {
        listener.acknowledge(data, acknowledgment);

        verify(acknowledgment, times(1)).acknowledge();
        verifyNoMoreInteractions(acknowledgment);
        verifyZeroInteractions(genericConsumer);
        verifyZeroInteractions(service);
    }

    /**
     * Test acknowledge
     */
    @Test
    public void testAcknowledgeWhenException() {
        doThrow(NullPointerException.class).when(acknowledgment).acknowledge();

        listener.acknowledge(data, acknowledgment);

        verify(acknowledgment, times(1)).acknowledge();
        verifyNoMoreInteractions(acknowledgment);
        verifyZeroInteractions(genericConsumer);
        verifyZeroInteractions(service);
    }


    /**
     * Test onMessage when the message is assigned with success the first time
     * 
     */
    @Test
    public void testOnMessage() throws Exception {
        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verifyZeroInteractions(onMsgConsumer);
        verify(service, times(1)).read(Mockito.eq(data), Mockito.eq(acknowledgment), Mockito.eq(genericConsumer), Mockito.eq(ProductCategory.AUXILIARY_FILES));
        verify(appStatus, times(1)).setWaiting();
    }

    /**
     * Test onMessage when the first assignment fails
     * 
     */
    @Test
    public void testOnMessageWhenFirstReadFails()
            throws Exception {
        doThrow(new AppCatalogMqiReadApiError(ProductCategory.AUXILIARY_FILES,
                "uri", "dto-object", "error-message")).when(service).read(Mockito.any(), Mockito.any(), Mockito.eq(genericConsumer), Mockito.eq(ProductCategory.AUXILIARY_FILES));

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verify(appStatus, times(1)).setError("MQI");
        verifyNoMoreInteractions(appStatus);
        verifyZeroInteractions(onMsgConsumer);
        verifyZeroInteractions(genericConsumer);
        verify(service, times(1)).read(Mockito.eq(data), Mockito.eq(acknowledgment), Mockito.eq(genericConsumer), Mockito.eq(ProductCategory.AUXILIARY_FILES));
    }
}
