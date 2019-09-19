package esa.s1pdgs.cpoc.archives;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Archives application
 * @author Olivier Bex-Chauvet
 *
 */
@SpringBootApplication
@EnableScheduling
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
