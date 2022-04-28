package esa.s1pdgs.cpoc.ingestion.filter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import esa.s1pdgs.cpoc.ingestion.filter.service.IngestionFilterService;

@Configuration
@PropertySource("classpath:stream-parameters.properties")
public class Configration {
	
	@Autowired
	IngestionFilterConfigurationProperties ingestionFilterConfigurationProperties;
	
	@Bean
	@Primary
	public IngestionFilterService getIngestionFilterService() {
		return new IngestionFilterService(ingestionFilterConfigurationProperties);
	}

}
