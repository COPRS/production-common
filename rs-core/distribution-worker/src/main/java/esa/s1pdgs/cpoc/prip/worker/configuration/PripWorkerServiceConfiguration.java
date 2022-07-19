package esa.s1pdgs.cpoc.prip.worker.configuration;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.worker.service.PripPublishingService;

@Configuration
public class PripWorkerServiceConfiguration {
	
	@Autowired
	private CommonConfigurationProperties commonProperties;
	
	@Autowired
	private ObsClient obsClient;
	
	@Autowired
	private MetadataClient metadataClient;
	
	@Autowired
	private PripMetadataRepository pripMetadataRepo;
	
	@Autowired
	private PripWorkerConfigurationProperties props;
	
	@Bean
	public Consumer<CompressionEvent> publish() {
		return new PripPublishingService(commonProperties, obsClient, metadataClient, pripMetadataRepo, props);
	}

}
