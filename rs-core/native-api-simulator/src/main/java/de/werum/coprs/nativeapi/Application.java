package de.werum.coprs.nativeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Reference System Native API Entrypoint
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties
@ComponentScan({ "de.werum.coprs.nativeapi", "esa.s1pdgs.cpoc.prip" })
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
