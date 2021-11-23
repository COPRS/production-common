package de.werum.coprs.ddip.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "de.werum.coprs.ddip.frontend")
@EnableConfigurationProperties
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
