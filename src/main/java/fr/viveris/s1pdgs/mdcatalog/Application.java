package fr.viveris.s1pdgs.mdcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ingestor application
 * @author Cyrielle Gailliard
 *
 */
@SpringBootApplication
@EnableScheduling
public class Application {
	
    /**
     * Main application
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
