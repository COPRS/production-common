package esa.s1pdgs.cpoc.ingestion;

import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {
    public static void main(String[] args) {
    	Random rand = new Random();
    	int port = 9000 + rand.nextInt(50000);
    	System.getProperties().put("server.port", port);
        SpringApplication.run(Application.class, args);
    }
}
