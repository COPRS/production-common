package esa.s1pdgs.cpoc.odip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TopicConfig {

    @Value("${kafka.topic}")
    private String topic;

    public String getTopic() {
        return topic;
    }
}
