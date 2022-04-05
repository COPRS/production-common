package esa.s1pdgs.cpoc.mdc.filter.config;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.mdc.filter.service.MetadataFilterService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

@Configuration
public class MetadataFilterServiceConfiguration {

	@Bean
	public Function<Message<?>, CatalogJob> convertToCatalogJob() {
		return new MetadataFilterService();
	}
}
