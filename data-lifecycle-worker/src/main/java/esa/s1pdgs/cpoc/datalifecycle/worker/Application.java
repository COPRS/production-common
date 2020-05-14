package esa.s1pdgs.cpoc.datalifecycle.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {

    /**
     * Main application
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
