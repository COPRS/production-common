package esa.s1pdgs.cpoc.reqrepo.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.message.MessageConsumerFactory;
import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;
import esa.s1pdgs.cpoc.reqrepo.consumption.ErrorQueueConsumer;
import esa.s1pdgs.cpoc.reqrepo.service.RequestRepository;


@Configuration
public class MessagingConfig {

    @Bean
    MessageConsumerFactory<FailedProcessingDto> consumerFactory(final RequestRepository requestRepository, final KafkaProperties kafkaProperties) {
        return () -> Collections.singletonList(
                new ErrorQueueConsumer(requestRepository, kafkaProperties.getErrorTopic()));
    }


}
