package esa.s1pdgs.cpoc.metadata.extraction.config;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.metadata.extraction.service.ExtractionService;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.MetadataExtractorFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

@Configuration
public class ExtractionServiceConfiguration {

	@Autowired
	private EsServices esServices;
	
	@Autowired
	private MdcWorkerConfigurationProperties properties;
	
	@Autowired
	private MetadataExtractorFactory factory;
	
	@Autowired
	private TimelinessConfiguration timelinessConfig;
	
	@Bean
	public Function<CatalogJob, CatalogEvent> extractMetadata() {
		return new ExtractionService(esServices, properties, factory, timelinessConfig);
	}
}
