package esa.s1pdgs.cpoc.errorrepo.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import esa.s1pdgs.cpoc.errorrepo.kafka.producer.KafkaSubmissionClient;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;


@EnableKafka
@Configuration
public class KafkaConfig {

    /**
     * URI of KAFKA cluster
     */
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    /**
     * Group identifier for KAFKA
     */
    @Value("${kafka.group-id}")
    private String kafkaGroupId;
    
    /**
     * Pool timeout for consumption
     */
    @Value("${kafka.poll-timeout}")
    private long kafkaPooltimeout;
    
    @Value("${kafka.max-pool-records}")
    private int kafkaMaxPoolRecords;
    
    @Value("${kafka.session-timeout-ms}")
    private int kafkaSessionTimeoutMs;
    
    @Value("${kafka.max-retries:10}")
    private int maxRetries;
    /**
     * Consumer configuration
     * 
     * @return
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaMaxPoolRecords);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                kafkaSessionTimeoutMs);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
        return props;
    }

    /**
     * Consumer factory
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
	@Bean
    public ConsumerFactory<String, FailedProcessingDto> consumerFactory() {
    	return new DefaultKafkaConsumerFactory<String,FailedProcessingDto>(consumerConfigs(), 
    			new StringDeserializer(),
                new JsonDeserializer<>(FailedProcessingDto.class));
    }

    /**
     * Listener containers factory
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
	@Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, FailedProcessingDto>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FailedProcessingDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
    
    
    @Bean
    public KafkaSubmissionClient kafkaProducerClient()
    {
        final Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.RETRIES_CONFIG, maxRetries);        
                
        return new KafkaSubmissionClient(
        		new KafkaTemplate<String, Object>(new DefaultKafkaProducerFactory<>(props))
        );
    }

}
