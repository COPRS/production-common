package standalone.appcatalog.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("esa.s1pdgs.cpoc.appcatalog")
public class StandaloneApplication {
    public static void main(String[] args) {
        SpringApplication.run(StandaloneApplication.class, args);
    }
}