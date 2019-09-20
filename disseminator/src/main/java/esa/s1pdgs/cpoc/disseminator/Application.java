package esa.s1pdgs.cpoc.disseminator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;

/**
 * Ingestor application
 * @author Faisal Rafi
 *
 */
@SpringBootApplication
@EnableConfigurationProperties({ObsConfigurationProperties.class})
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {
	
    /**
     * Main application
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
