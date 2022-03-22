package esa.s1pdgs.cpoc.validation.rest;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// dirty-workaround warning: this is for 'ValidationRestControllerTest' to resolve RestTemplateBuilder...
@Configuration
public class ValidationRestControllerTestConfig {
	
	@Bean
	public RestTemplateBuilder rtb() {
		return new RestTemplateBuilder();
	}

}
