package esa.s1pdgs.cpoc.disseminator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;

/**
 * Ingestor application
 * @author Faisal Rafi
 *
 */
@SpringBootApplication
@EnableConfigurationProperties({ObsConfigurationProperties.class, DisseminationProperties.class})
public class Application {
	
    /**
     * Main application
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
