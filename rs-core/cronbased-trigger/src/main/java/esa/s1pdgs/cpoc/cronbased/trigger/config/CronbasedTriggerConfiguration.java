package esa.s1pdgs.cpoc.cronbased.trigger.config;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.cronbased.trigger.db.CronbasedTriggerEntryRepository;
import esa.s1pdgs.cpoc.cronbased.trigger.service.CronbasedTriggerService;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Configuration
public class CronbasedTriggerConfiguration {

	@Autowired
	private CronbasedTriggerProperties properties;
	
	@Autowired
	private MetadataClient metadataClient;
	
	@Autowired
	private CronbasedTriggerEntryRepository repository;
	
	@Autowired
	private ObsClient obsClient;

	@Bean
	public Function<Message<?>, List<Message<CatalogEvent>>> cronbasedTrigger() {
		return new CronbasedTriggerService(properties, metadataClient, repository, obsClient);
	}
}
