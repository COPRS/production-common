package esa.s1pdgs.cpoc.disseminator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Ingestor application
 * @author Faisal Rafi
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = {"esa.s1pdgs.cpoc.obs_sdk"})
public class Application {
	
    /**
     * Main application
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
