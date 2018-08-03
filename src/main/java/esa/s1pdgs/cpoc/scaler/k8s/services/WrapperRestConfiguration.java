package esa.s1pdgs.cpoc.scaler.k8s.services;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WrapperRestConfiguration {

	@Bean(name = "restWrapperTemplate")
	public RestTemplate restWrapperTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
}
