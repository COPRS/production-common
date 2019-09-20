package esa.s1pdgs.cpoc.mdcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;

/**
 * Ingestor application
 * @author Cyrielle Gailliard
 *
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ObsConfigurationProperties.class})
public class Application {
	
    /**
     * Main application
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
