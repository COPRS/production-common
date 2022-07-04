package esa.s1pdgs.cpoc.reqrepo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import esa.s1pdgs.cpoc.message.kafka.config.KafkaProducerConfiguration;

@Configuration
@Import(KafkaProducerConfiguration.class)
public class KafkaConfiguration {

}
