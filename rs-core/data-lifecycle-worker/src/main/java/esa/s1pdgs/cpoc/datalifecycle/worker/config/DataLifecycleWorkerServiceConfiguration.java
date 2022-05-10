package esa.s1pdgs.cpoc.datalifecycle.worker.config;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.worker.service.CatalogEventService;
import esa.s1pdgs.cpoc.datalifecycle.worker.service.CompressionEventService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;

@Configuration
public class DataLifecycleWorkerServiceConfiguration {
	
	@Autowired
	private DataLifecycleWorkerConfigurationProperties configurationProperties;
	
	@Autowired
	private DataLifecycleMetadataRepository metadataRepo;
	
	@Bean
	public Consumer<CatalogEvent> update() {
		return new CatalogEventService(configurationProperties, metadataRepo);
	}
	
	@Bean
	public Consumer<CompressionEvent> updateCompressed() {
		return new CompressionEventService(configurationProperties, metadataRepo);
	}

}
