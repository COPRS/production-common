package esa.s1pdgs.cpoc.inbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import esa.s1pdgs.cpoc.inbox.config.InboxPollingConfigurationProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(InboxPollingConfigurationProperties.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
