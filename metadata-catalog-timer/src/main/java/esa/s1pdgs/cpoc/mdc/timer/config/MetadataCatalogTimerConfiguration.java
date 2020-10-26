package esa.s1pdgs.cpoc.mdc.timer.config;

import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import esa.s1pdgs.cpoc.mdc.timer.config.MetadataCatalogTimerSettings.TimerProperties;
import esa.s1pdgs.cpoc.mdc.timer.db.CatalogEventTimerEntryRepository;
import esa.s1pdgs.cpoc.mdc.timer.dispatcher.CatalogEventDispatcher;
import esa.s1pdgs.cpoc.mdc.timer.dispatcher.CatalogEventDispatcherImpl;
import esa.s1pdgs.cpoc.mdc.timer.publish.KafkaPublisher;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

@Configuration
public class MetadataCatalogTimerConfiguration {

	private static final Logger LOGGER = LogManager.getLogger(MetadataCatalogTimerConfiguration.class);

	private MetadataCatalogTimerSettings settings;
	private MetadataClient metadataClient;
	private CatalogEventTimerEntryRepository repository;
	private KafkaTemplate<String, CatalogEvent> kafkaTemplate;
	private ThreadPoolTaskScheduler scheduler;

	@Autowired
	public MetadataCatalogTimerConfiguration(final MetadataCatalogTimerSettings settings,
			final MetadataClient metadataClient, final CatalogEventTimerEntryRepository repository,
			final KafkaTemplate<String, CatalogEvent> kafkaTemplate, final ThreadPoolTaskScheduler scheduler) {
		this.settings = settings;
		this.metadataClient = metadataClient;
		this.repository = repository;
		this.kafkaTemplate = kafkaTemplate;
		this.scheduler = scheduler;
	}

	@PostConstruct
	@Autowired
	public void setupCatalogEventDispatcher() {
		Set<Entry<String, TimerProperties>> entries = settings.getConfig().entrySet();

		for (Entry<String, TimerProperties> entry : entries) {
			TimerProperties config = entry.getValue();
			CatalogEventDispatcher dispatcher = new CatalogEventDispatcherImpl(metadataClient, repository,
					new KafkaPublisher(kafkaTemplate, config.getTopic()), entry.getKey(), config.getFamily());

			CronTrigger cronTrigger = new CronTrigger(config.getCron());
			scheduler.schedule(dispatcher, cronTrigger);
			LOGGER.info("Started new CatalogEventDispatcher ({}, {}, {})", entry.getKey(), config.getFamily(),
					config.getCron());
		}
	}

}
