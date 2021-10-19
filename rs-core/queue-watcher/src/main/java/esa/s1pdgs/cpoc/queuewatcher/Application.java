package esa.s1pdgs.cpoc.queuewatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ingestor application
 * @author Faisal Rafi
 *
 */
@SpringBootApplication
@EnableScheduling
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
