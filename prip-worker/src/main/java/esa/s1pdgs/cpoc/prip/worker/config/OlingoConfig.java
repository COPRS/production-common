package esa.s1pdgs.cpoc.prip.worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.prip.worker.service.edm.EdmProvider;

@Configuration
public class OlingoConfig {

	@Bean
	EdmProvider getEdmProvider() {
		return new EdmProvider();
	}

}
