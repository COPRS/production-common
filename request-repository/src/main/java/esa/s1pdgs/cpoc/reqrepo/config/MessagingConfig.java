package esa.s1pdgs.cpoc.reqrepo.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.message.MessageConsumerFactory;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;
import esa.s1pdgs.cpoc.reqrepo.kafka.consumption.ErrorQueueConsumer;
import esa.s1pdgs.cpoc.reqrepo.kafka.producer.MessageSubmissionClient;
import esa.s1pdgs.cpoc.reqrepo.service.RequestRepository;


@Configuration
public class MessagingConfig {

    @Bean
    public MessageSubmissionClient kafkaProducerClient(MessageProducer<Object> messageProducer) {
        return new MessageSubmissionClient(messageProducer);
    }

    @Bean
    MessageConsumerFactory<FailedProcessingDto> consumerFactory(final RequestRepository requestRepository, final KafkaProperties kafkaProperties) {
        return () -> Collections.singletonList(
                new ErrorQueueConsumer(requestRepository, kafkaProperties.getErrorTopic()));
    }


}
