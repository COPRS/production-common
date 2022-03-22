package esa.s1pdgs.cpoc.message.kafka.config;

import java.util.Collections;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.message.MessageConsumerFactory;

@SpringBootApplication
public class TestApplication {
    @Configuration
    public static class TestConfig {
        @Bean
        public MessageConsumerFactory<Object> emptyFactory() {
            return Collections::emptyList;
        }
    }

}
