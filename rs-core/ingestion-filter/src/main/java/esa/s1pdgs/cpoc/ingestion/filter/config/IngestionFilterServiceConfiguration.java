package esa.s1pdgs.cpoc.ingestion.filter.config;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.ingestion.filter.service.IngestionFilterService;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@Configuration
public class IngestionFilterServiceConfiguration {
	
	@Autowired
	private IngestionFilterConfigurationProperties properties;
	
	@Bean
	public Function<List<IngestionJob>, List<IngestionJob>> filter() {
		return new IngestionFilterService(properties);
	}
}
