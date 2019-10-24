package esa.s1pdgs.cpoc.prip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
