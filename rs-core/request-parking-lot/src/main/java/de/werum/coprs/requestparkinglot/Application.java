package de.werum.coprs.requestparkinglot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@SpringBootApplication
@EnableMongoRepositories
@EnableConfigurationProperties
@ComponentScan({"de.werum.coprs", "esa.s1pdgs.cpoc"})
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
