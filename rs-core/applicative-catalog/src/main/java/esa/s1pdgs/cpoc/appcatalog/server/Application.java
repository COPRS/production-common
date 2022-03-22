package esa.s1pdgs.cpoc.appcatalog.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Applicative catalog application
 * 
 * @author Viveris Technologies
 *
 */
@SpringBootApplication
@EnableScheduling
@EnableMongoRepositories
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {
	
    /**
     * Main application
     * @param args
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
