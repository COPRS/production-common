package esa.s1pdgs.cpoc.scaler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application
 * @author Cyrielle Gailliard
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
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
