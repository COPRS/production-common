package esa.s1pdgs.cpoc.mqi.server;

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

import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

public class GenericKafkaUtils<T> {

    public final static String TOPIC_ERROR = "t-pdgs-errors";
    public final static String TOPIC_L0_JOBS = "t-pdgs-aio-execution-jobs";
    public final static String TOPIC_L0_PRODUCTS = "t-pdgs-l0-slices";
    public final static String TOPIC_L0_ACNS = "t-pdgs-l0-acns";
    public final static String TOPIC_L0_REPORTS = "t-pdgs-l0-reports";
    public final static String TOPIC_L1_JOBS = "t-pdgs-l1-jobs";
    public final static String TOPIC_L1_PRODUCTS = "t-pdgs-l1-slices";
    public final static String TOPIC_L1_ACNS = "t-pdgs-l1-acns";
    public final static String TOPIC_L1_REPORTS = "t-pdgs-l1-reports";
    public final static String TOPIC_EDRS_SESSIONS = "t-pdgs-session-file-ingestion-events";
    public final static String TOPIC_AUXILIARY_FILES = "t-pdgs-aux-ingestion-events";
    public final static String TOPIC_L0_SEGMENTS = "t-pdgs-l0-segments";
    
    public final static String TOPIC_L2_JOBS = "t-pdgs-l2-jobs";
    public final static String TOPIC_L2_PRODUCTS = "t-pdgs-l2-slices";
    public final static String TOPIC_L2_ACNS = "t-pdgs-l2-acns";
    public final static String TOPIC_L2_REPORTS = "t-pdgs-l2-reports";
    
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

    public ConsumerRecord<String, IngestionEvent> getReceivedRecordEdrsSession(
            String topic) throws Exception {
        Consumer<String, IngestionEvent> consumer =
                new DefaultKafkaConsumerFactory<String, IngestionEvent>(
                        consumerProps(), new StringDeserializer(),
                        new JsonDeserializer<>(IngestionEvent.class)).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
        return KafkaTestUtils.getSingleRecord(consumer, topic);
    }

    public ConsumerRecord<String, ProductionEvent> getReceivedRecordAux(
            String topic) throws Exception {
        Consumer<String, ProductionEvent> consumer =
                new DefaultKafkaConsumerFactory<String, ProductionEvent>(
                        consumerProps(), new StringDeserializer(),
                        new JsonDeserializer<>(ProductionEvent.class)).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
        return KafkaTestUtils.getSingleRecord(consumer, topic);
    }

    public ConsumerRecord<String, IpfExecutionJob> getReceivedRecordJobs(
            String topic) throws Exception {
        Consumer<String, IpfExecutionJob> consumer =
                new DefaultKafkaConsumerFactory<String, IpfExecutionJob>(
                        consumerProps(), new StringDeserializer(),
                        new JsonDeserializer<>(IpfExecutionJob.class)).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
        return KafkaTestUtils.getSingleRecord(consumer, topic);
    }

    public ConsumerRecord<String, ProductionEvent> getReceivedRecordProducts(
            String topic) throws Exception {
        Consumer<String, ProductionEvent> consumer =
                new DefaultKafkaConsumerFactory<String, ProductionEvent>(
                        consumerProps(), new StringDeserializer(),
                        new JsonDeserializer<>(ProductionEvent.class)).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
        return KafkaTestUtils.getSingleRecord(consumer, topic);
    }

    public ConsumerRecord<String, ProductionEvent> getReceivedRecordSegments(
            String topic) throws Exception {
        Consumer<String, ProductionEvent> consumer =
                new DefaultKafkaConsumerFactory<String, ProductionEvent>(
                        consumerProps(), new StringDeserializer(),
                        new JsonDeserializer<>(ProductionEvent.class)).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
        return KafkaTestUtils.getSingleRecord(consumer, topic);
    }

    public ConsumerRecord<String, LevelReportDto> getReceivedRecordReports(
            String topic) throws Exception {
        Consumer<String, LevelReportDto> consumer =
                new DefaultKafkaConsumerFactory<String, LevelReportDto>(
                        consumerProps(), new StringDeserializer(),
                        new JsonDeserializer<>(LevelReportDto.class)).createConsumer();
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
