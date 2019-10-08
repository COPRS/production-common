package esa.s1pdgs.cpoc.prip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.prip.service.EdmProvider;

@Configuration
public class OlingoConfig {

	@Bean
	EdmProvider getEdmProvider() {
		return new EdmProvider();
	}

}
