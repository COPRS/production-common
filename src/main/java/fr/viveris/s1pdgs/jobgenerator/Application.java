package fr.viveris.s1pdgs.jobgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * L0 job generator application
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
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
