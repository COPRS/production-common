package esa.s1pdgs.cpoc.ingestion.trigger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@ComponentScan({"esa.s1pdgs.cpoc","de.werum.coprs"})
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
