package esa.s1pdgs.cpoc.ipf.preparation.trigger.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.KafkaErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

@Configuration
public class ErrorRepoConfiguration {
	
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${kafka.max-retries}")
    private int maxRetries;
    
    @Value("${kafka.error-topic}")
    private String topic;

	private KafkaTemplate<String, FailedProcessingDto> kafkaTemplate()
	{
        final Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.RETRIES_CONFIG, maxRetries);        
        return new KafkaTemplate<String, FailedProcessingDto>(new DefaultKafkaProducerFactory<>(props));
	}
	
	@Bean 
	public ErrorRepoAppender kafkaErrorRepoAppender()
	{
		return new KafkaErrorRepoAppender(kafkaTemplate(), topic);
	}
}
