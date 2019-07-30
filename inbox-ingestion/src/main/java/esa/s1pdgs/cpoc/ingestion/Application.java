package esa.s1pdgs.cpoc.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import esa.s1pdgs.cpoc.ingestion.config.IngestionServiceConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IngestionServiceConfigurationProperties.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
