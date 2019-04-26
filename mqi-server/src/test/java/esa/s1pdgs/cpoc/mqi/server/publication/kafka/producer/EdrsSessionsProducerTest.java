package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

/**
 * Test the producer LevelJobProducerTest
 * 
 * @author Viveris Technologies
 */
public class EdrsSessionsProducerTest {

    /**
     * Exception
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Kafka properties
     */
    @Mock
    private KafkaProperties properties;

    /**
     * Kafka template
     */
    @Mock
    private KafkaTemplate<String, EdrsSessionDto> template;

    @Mock
    private ListenableFuture<SendResult<String, EdrsSessionDto>> future;

    /**
     * Publisher to test
     */
    private EdrsSessionsProducer producer;

    /**
     * DTO to send
     */
    private EdrsSessionDto dto;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        dto = new EdrsSessionDto("product-name-1", 2, EdrsSessionFileType.RAW, "S1",
                "A");

        producer = new EdrsSessionsProducer(properties, template);
    }

    /**
     * Test send when exceptions
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws MqiPublicationError
     */
    @Test
    public void sendWhenCancellationException() throws InterruptedException,
            ExecutionException, MqiPublicationError {

        doThrow(CancellationException.class).when(template)
                .send(Mockito.anyString(), Mockito.any());

        thrown.expect(MqiPublicationError.class);
        thrown.expect(hasProperty("topic", is("topic-name")));
        thrown.expect(hasProperty("productName", is("product-name-1")));
        thrown.expect(hasProperty("dto", is(dto)));

        producer.send("topic-name", dto);
    }

    /**
     * Test send when exceptions
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws MqiPublicationError
     */
    @Test
    public void sendWhenInterruptedException() throws InterruptedException,
            ExecutionException, MqiPublicationError {

        doReturn(future).when(template).send(Mockito.anyString(),
                Mockito.any());
        doThrow(InterruptedException.class).when(future).get();

        thrown.expect(MqiPublicationError.class);
        thrown.expect(hasProperty("topic", is("topic-name")));
        thrown.expect(hasProperty("productName", is("product-name-1")));
        thrown.expect(hasProperty("dto", is(dto)));

        producer.send("topic-name", dto);
    }

    /**
     * Test send when exceptions
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws MqiPublicationError
     */
    @Test
    public void sendWhenExecutionException() throws InterruptedException,
            ExecutionException, MqiPublicationError {

        doReturn(future).when(template).send(Mockito.anyString(),
                Mockito.any());
        doThrow(ExecutionException.class).when(future).get();

        thrown.expect(MqiPublicationError.class);
        thrown.expect(hasProperty("topic", is("topic-name")));
        thrown.expect(hasProperty("productName", is("product-name-1")));
        thrown.expect(hasProperty("dto", is(dto)));

        producer.send("topic-name", dto);

    }

    /**
     * Test nominal send
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws MqiPublicationError
     */
    @Test
    public void send() throws InterruptedException, ExecutionException,
            MqiPublicationError {

        doReturn(future).when(template).send(Mockito.anyString(),
                Mockito.any());
        doReturn(null).when(future).get();

        producer.send("topic-name", dto);
        verify(template, times(1)).send(Mockito.eq("topic-name"),
                Mockito.eq(dto));
    }

}
