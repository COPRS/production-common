package esa.s1pdgs.cpoc.errorrepo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.MessageErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;

@Configuration
public class ErrorRepoConfiguration {
	
	@Bean
	@ConditionalOnProperty("kafka.producer.max-retries")
	public ErrorRepoAppender kafkaErrorRepoAppender(final MessageProducer<FailedProcessingDto> messageProducer, final KafkaProperties kafkaProperties)
	{
		return new MessageErrorRepoAppender(kafkaProperties.getErrorTopic(), messageProducer);
	}
}