package fr.viveris.s1pdgs.mqi.server.consumption.kafka.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import fr.viveris.s1pdgs.mqi.model.queue.AuxiliaryFileDto;
import fr.viveris.s1pdgs.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.mqi.server.GenericKafkaUtils;
import fr.viveris.s1pdgs.mqi.server.KafkaProperties;
import fr.viveris.s1pdgs.mqi.server.consumption.kafka.consumer.GenericConsumer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

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

    @ClassRule
    public static KafkaEmbedded embeddedKafka =
            new KafkaEmbedded(1, false, CONSUMER_TOPIC, GenericKafkaUtils.TOPIC_ERROR);

    @Test
    public void testConstructor() {
        GenericConsumer<AuxiliaryFileDto> consumer = new GenericConsumer<>(
                properties, CONSUMER_TOPIC, AuxiliaryFileDto.class);
        assertEquals(CONSUMER_TOPIC, consumer.getTopic());
        assertNull(consumer.getConsumedMessage());
        assertEquals(AuxiliaryFileDto.class, consumer.getConsumedMsgClass());
    }

    @Test
    public void testAuxiliaryFilesConsumer()
            throws InterruptedException, ExecutionException {
        AuxiliaryFileDto dto = new AuxiliaryFileDto("product-name", "key-obs");
        GenericMessageDto<AuxiliaryFileDto> genDto1 =
                new GenericMessageDto<AuxiliaryFileDto>(
                        Objects.hash(CONSUMER_TOPIC, 1), CONSUMER_TOPIC, dto);
        AuxiliaryFileDto dto2 =
                new AuxiliaryFileDto("product-name-2", "key-obs-2");
        GenericMessageDto<AuxiliaryFileDto> genDto2 =
                new GenericMessageDto<AuxiliaryFileDto>(
                        Objects.hash(CONSUMER_TOPIC, 2), CONSUMER_TOPIC, dto2);
        GenericKafkaUtils<AuxiliaryFileDto> kafkaUtils =
                new GenericKafkaUtils<>(embeddedKafka);

        GenericConsumer<AuxiliaryFileDto> consumer = new GenericConsumer<>(
                properties, CONSUMER_TOPIC, AuxiliaryFileDto.class);
        consumer.start();
        assertNull("No read message", consumer.getConsumedMessage());

        Thread.sleep(5000);

        // Send first DTO
        kafkaUtils.sendMessageToKafka(dto, CONSUMER_TOPIC);
        await().atMost(1, SECONDS).until(() -> consumer.getConsumedMessage(),
                is(genDto1));
        assertNotNull("A message should be read",
                consumer.getConsumedMessage());
        assertEquals(genDto1, consumer.getConsumedMessage());
        assertTrue(consumer.isPaused());

        // Send second DTO without resuming consumer
        kafkaUtils.sendMessageToKafka(dto2, CONSUMER_TOPIC);
        Thread.sleep(1000);
        assertEquals(genDto1, consumer.getConsumedMessage());
        assertTrue(consumer.isPaused());

        // REsume consumer
        consumer.resume();
        Thread.sleep(1000);
        assertEquals(genDto2, consumer.getConsumedMessage());

    }
}
