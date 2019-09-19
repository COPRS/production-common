package esa.s1pdgs.cpoc.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import esa.s1pdgs.cpoc.ingestion.config.IngestionServiceConfigurationProperties;

@SpringBootApplication
@ComponentScan(basePackages = {"esa.s1pdgs.cpoc.obs_sdk"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
