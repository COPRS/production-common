package esa.s1pdgs.cpoc.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import esa.s1pdgs.cpoc.ingestion.config.IngestionServiceConfigurationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ObsConfigurationProperties.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
