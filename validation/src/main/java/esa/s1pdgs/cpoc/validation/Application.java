package esa.s1pdgs.cpoc.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * A service that allows to perform inconsistency checks between
 * OBS and metadata catalog service.
 * 
 * @author florian_sievert
 *
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application {
	/**
     * Main application
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
