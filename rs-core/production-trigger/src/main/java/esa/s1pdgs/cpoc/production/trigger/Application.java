package esa.s1pdgs.cpoc.production.trigger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * L0 job generator application
 * @author Cyrielle Gailliard
 *
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
