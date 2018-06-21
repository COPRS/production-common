package fr.viveris.s1pdgs.level0.wrapper.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.ObsService;
import fr.viveris.s1pdgs.level0.wrapper.test.MockPropertiesTest;

public class AbstractJobConsumerTest extends MockPropertiesTest {

    /**
     * Service for accessing to the OBS
     */
    @Mock
    private ObsService obsService;

    /**
     * Factory for producing output messages in KAFKA topics
     */
    @Mock
    private OutputProcuderFactory outputProcuder;

    /**
     * Kafka endpoint registry used to pause the consumer
     */
    @Mock
    private KafkaListenerEndpointRegistry consumersRegistry;

    /**
     * Spring consumer
     */
    @Mock
    private MessageListenerContainer listenerContainer;

    /**
     * Acknowledgement
     */
    @Mock
    private Acknowledgment ack;

    /**
     * Consumer to test
     */
    private L0JobConsumer consumerL0;

    /**
     * Consumer to test
     */
    private L1JobConsumer consumerL1;
    
    /**
     * Received job
     */
    private JobDto job = new JobDto("product-id", "work-directory", "job-order");

    /**
     * Initialization
     */
    @Before
    public void init() {
        // Init mock
        MockitoAnnotations.initMocks(this);

        // Mock
        mockDefaultAppProperties();
        mockDefaultDevProperties();
        mockDefaultStatus();
        doReturn(listenerContainer).when(consumersRegistry)
                .getListenerContainer(Mockito.anyString());
        doNothing().when(listenerContainer).pause();
        doNothing().when(ack).acknowledge();

        // Build consumer
        consumerL0 = new L0JobConsumer(obsService, outputProcuder, properties,
                devProperties, appStatus, consumersRegistry, TOPIC_NAME);
        consumerL1 = new L1JobConsumer(obsService, outputProcuder, properties,
                devProperties, appStatus, consumersRegistry, TOPIC_NAME);

    }

    /**
     * Checl L0 consumer construction
     */
    public void testBuildL0Consumer() {
        assertEquals(TOPIC_NAME, consumerL0.getTopic());
        assertEquals(L0JobConsumer.CONSUMER_ID,
                consumerL0.getSpringConsumerId());
        assertEquals(0, consumerL0.nbCurrentTasks);
    }

    /**
     * Checl L1 consumer construction
     */
    public void testBuildL1Consumer() {
        assertEquals(TOPIC_NAME, consumerL1.getTopic());
        assertEquals(L1JobConsumer.CONSUMER_ID,
                consumerL1.getSpringConsumerId());
        assertEquals(0, consumerL1.nbCurrentTasks);
    }

    /**
     * Test ack message when exception raised
     */
    @Test
    public void testAckMessageWhenException() {
        doThrow(new IllegalArgumentException("error message")).when(ack)
                .acknowledge();
        consumerL0.ackMessage(ack, "prefix-log");

        verify(appStatus, times(1)).setError();
        verify(ack, times(1)).acknowledge();
    }

    /**
     * Test ack message
     */
    @Test
    public void testAckMessage() {
        consumerL0.ackMessage(ack, "prefix-log");

        verify(appStatus, never()).setError();
        verify(ack, times(1)).acknowledge();
    }

    /**
     * Test pause consumer when invalid id
     */
    @Test
    public void testPauseWhenInvalidConsumerId() {
        doReturn(null).when(consumersRegistry)
                .getListenerContainer(Mockito.anyString());

        consumerL0.pauseConsumer("prefix-log");

        verify(listenerContainer, never()).pause();
        verify(consumersRegistry, times(1))
                .getListenerContainer(Mockito.eq(L0JobConsumer.CONSUMER_ID));
    }

    /**
     * Test pause consumer when invalid id
     */
    @Test
    public void testPauseNominal() {
        consumerL0.pauseConsumer("prefix-log");

        verify(listenerContainer, times(1)).pause();
        verify(consumersRegistry, times(1))
                .getListenerContainer(Mockito.eq(L0JobConsumer.CONSUMER_ID));
    }
    
    /**
     * Test when application shall be stopped
     */
    @Test
    public void testWhenAppShallBeStopped() {
        doReturn(true).when(appStatus).isShallBeStopped();
        
        consumerL0.internalReceive(job, ack, "result.list");

        verify(appStatus, times(1)).forceStopping();
        verifyZeroInteractions(ack);
        assertEquals(0, consumerL0.nbCurrentTasks);
    }
    
    /**
     * Test when job shall be launched
     */
    @Test
    public void testInternalReceived() {
        
        mockTmAppProperties(1, 1, 1, 1);
        
        consumerL0.internalReceive(job, ack, "result.list");

        verify(appStatus, times(1)).setProcessing();
        verify(appStatus, never()).setError();
        verify(ack, times(1)).acknowledge();
        verify(listenerContainer, times(1)).pause();
        assertEquals(1, consumerL0.nbCurrentTasks);
        
    }
    
    /**
     * Test when job shall be launched
     */
    @Test
    public void testL0Receive() {
        
        mockTmAppProperties(1, 1, 1, 1);
        
        consumerL0.receive(job, ack);

        verify(appStatus, times(1)).setProcessing();
        verify(appStatus, never()).setError();
        verify(ack, times(1)).acknowledge();
        verify(listenerContainer, times(1)).pause();
        assertEquals(1, consumerL0.nbCurrentTasks);
        
    }
    
    /**
     * Test when job shall be launched
     */
    @Test
    public void testL1Received() {
        
        mockTmAppProperties(1, 1, 1, 1);
        
        consumerL1.receive(job, ack);

        verify(appStatus, times(1)).setProcessing();
        verify(appStatus, never()).setError();
        verify(ack, times(1)).acknowledge();
        verify(listenerContainer, times(1)).pause();
        assertEquals(1, consumerL1.nbCurrentTasks);
        
    }
    
    /**
     * Test when job shall be launched
     */
    @Test
    public void testInternalReceivedWhenException() {
        doThrow(new IllegalArgumentException("test error")).when(appStatus).setProcessing();
        
        consumerL0.internalReceive(job, ack, "result.list");

        verify(appStatus, times(1)).setProcessing();
        verify(appStatus, times(1)).setError();
        verify(ack, never()).acknowledge();
        verify(listenerContainer, never()).pause();
        assertEquals(0, consumerL0.nbCurrentTasks);
        
    }

}
