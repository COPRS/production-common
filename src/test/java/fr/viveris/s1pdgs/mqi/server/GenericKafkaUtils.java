package fr.viveris.s1pdgs.mqi.server;

import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericKafkaUtils<T> {

    public final static String TOPIC_ERROR = "t-pdgs-errors";
    public final static String TOPIC_L0_JOBS = "t-pdgs-l0-jobs";
    public final static String TOPIC_EDRS_SESSIONS = "t-pdgs-edrs-sessions";

    private final KafkaEmbedded embeddedKafka;

    public GenericKafkaUtils(final KafkaEmbedded embeddedKafka) {
        this.embeddedKafka = embeddedKafka;
    }

    public ConsumerRecord<String, T> getReceivedRecord(String topic)
            throws Exception {
        Consumer<String, T> consumer =
                new DefaultKafkaConsumerFactory<String, T>(consumerProps())
                        .createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
        return KafkaTestUtils.getSingleRecord(consumer, topic);
    }

    public void sendMessageToKafka(T message, String topic)
            throws InterruptedException,
            java.util.concurrent.ExecutionException {
        Producer<String, T> producer =
                new DefaultKafkaProducerFactory<String, T>(producerProps())
                        .createProducer();
        producer.send(new ProducerRecord<>(topic, message)).get();
    }

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                UUID.randomUUID().toString(), "true", embeddedKafka);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        return props;
    }

    private Map<String, Object> producerProps() {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafka);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        return props;
    }

    public static String convertObjectToJsonString(Object dto)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(dto);
    }
}
