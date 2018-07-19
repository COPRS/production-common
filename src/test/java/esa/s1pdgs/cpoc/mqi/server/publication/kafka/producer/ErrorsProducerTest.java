package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

public class ErrorsProducerTest {

    /**
     * Kafka properties
     */
    @Mock
    private KafkaProperties properties;

    /**
     * Kafka template
     */
    @Mock
    private KafkaTemplate<String, String> template;

    @Mock
    private ListenableFuture<SendResult<String, String>> future;

    /**
     * Publisher to test
     */
    private ErrorsProducer producer;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        producer = new ErrorsProducer(properties, "topic", template);
    }

    /**
     * Test send when exceptions
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void sendWhenException()
            throws InterruptedException, ExecutionException {
        doThrow(CancellationException.class).when(template)
                .send(Mockito.anyString(), Mockito.anyString());
        assertFalse(producer.send("message to send1"));
        verify(template, times(1)).send(Mockito.eq("topic"),
                Mockito.eq("message to send1"));

        doReturn(future).when(template).send(Mockito.anyString(),
                Mockito.anyString());
        doThrow(InterruptedException.class).when(future).get();
        assertFalse(producer.send("message to send2"));
        verify(template, times(1)).send(Mockito.eq("topic"),
                Mockito.eq("message to send2"));

        doReturn(future).when(template).send(Mockito.anyString(),
                Mockito.anyString());
        doThrow(ExecutionException.class).when(future).get();
        assertFalse(producer.send("message to send3"));
        verify(template, times(1)).send(Mockito.eq("topic"),
                Mockito.eq("message to send3"));
    }

    /**
     * Test nominal send
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void send() throws InterruptedException, ExecutionException {

        doReturn(future).when(template).send(Mockito.anyString(),
                Mockito.anyString());
        doReturn(null).when(future).get();
        assertTrue(producer.send("message to send"));
        verify(template, times(1)).send(Mockito.eq("topic"),
                Mockito.eq("message to send"));
    }

}
