package esa.s1pdgs.cpoc.ingestor.kafka;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;

/**
 * Test the publication service using Kafka
 * 
 * @author Cyrielle Gailliard
 */
public class AbstractKafkaServiceTest {

    /**
     * Topic name
     */
    private static final String TOPIC_NAME = "topic-name";

    /**
     * KAFKA client
     */
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Mock future corresponding to the return of the Kafka send
     */
    @Mock
    private ListenableFuture<SendResult<String, String>> sendResult;

    /**
     * Service to test
     */
    private KafkaServiceImpl service;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        doReturn(sendResult).when(kafkaTemplate).send(Mockito.anyString(),
                Mockito.any());

        service = new KafkaServiceImpl(kafkaTemplate, TOPIC_NAME);
    }

    /**
     * Test when send OK
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws KafkaSendException
     */
    @Test
    public void testSendOk() throws InterruptedException, ExecutionException,
            MqiPublicationError {
        doReturn(null).when(sendResult).get();

        service.send("string to send");
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.eq(TOPIC_NAME), Mockito.eq("string to send"));
        Mockito.verify(sendResult, Mockito.times(1)).get();
    }

    /**
     * Test when kafka function raises a CancellationException exception
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws KafkaSendException
     */
    @Test
    public void testSendWhenCancellationException() throws InterruptedException,
            ExecutionException, MqiPublicationError {
        doThrow(new CancellationException("cancel exception raised"))
                .when(sendResult).get();

        thrown.expect(MqiPublicationError.class);
        thrown.expect(hasProperty("topic", is(TOPIC_NAME)));
        thrown.expect(hasProperty("productName", is("string to send")));
        thrown.expect(hasProperty("dto", is("string to send")));
        thrown.expectMessage("cancel exception raised");
        thrown.expectCause(isA(CancellationException.class));
        service.send("string to send");
    }

    /**
     * Test when kafka function raises a InterruptedException exception
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws KafkaSendException
     */
    @Test
    public void testSendWhenInterruptedException() throws InterruptedException,
            ExecutionException, MqiPublicationError {
        doThrow(new InterruptedException("cancel exception raised"))
                .when(sendResult).get();

        thrown.expect(MqiPublicationError.class);
        thrown.expect(hasProperty("topic", is(TOPIC_NAME)));
        thrown.expect(hasProperty("productName", is("string to send")));
        thrown.expect(hasProperty("dto", is("string to send")));
        thrown.expectMessage("cancel exception raised");
        thrown.expectCause(isA(InterruptedException.class));
        service.send("string to send");
    }

    /**
     * Test when kafka function raises a ExecutionException exception
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws KafkaSendException
     */
    @Test
    public void testSendWhenExecutionException() throws InterruptedException,
            ExecutionException, MqiPublicationError {
        doThrow(ExecutionException.class).when(sendResult).get();

        thrown.expect(MqiPublicationError.class);
        thrown.expect(hasProperty("topic", is(TOPIC_NAME)));
        thrown.expect(hasProperty("productName", is("string to send")));
        thrown.expect(hasProperty("dto", is("string to send")));
        thrown.expectCause(isA(ExecutionException.class));
        service.send("string to send");
    }
}

/**
 * Implementation class of Kafka service for tests
 */
class KafkaServiceImpl extends AbstractKafkaService<String> {

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param kafkaTopic
     */
    public KafkaServiceImpl(final KafkaTemplate<String, String> kafkaTemplate,
            final String kafkaTopic) {
        super(kafkaTemplate, kafkaTopic);
    }

    /**
     * 
     */
    @Override
    protected String extractProductName(final String obj) {
        return obj;
    }

}