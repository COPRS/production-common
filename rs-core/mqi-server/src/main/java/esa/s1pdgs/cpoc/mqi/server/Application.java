package esa.s1pdgs.cpoc.mqi.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * MQI application
 * 
 * @author Viveris Technologies
 *
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {
	
    /**
     * Main application
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
