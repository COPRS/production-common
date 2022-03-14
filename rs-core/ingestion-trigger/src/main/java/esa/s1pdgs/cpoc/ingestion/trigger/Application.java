package esa.s1pdgs.cpoc.ingestion.trigger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableConfigurationProperties
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
