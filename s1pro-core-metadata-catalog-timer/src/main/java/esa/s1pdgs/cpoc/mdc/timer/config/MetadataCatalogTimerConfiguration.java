package esa.s1pdgs.cpoc.mdc.timer.config;

import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

	private MetadataCatalogTimerSettings settings;

	@Autowired
	public MetadataCatalogTimerConfiguration(final MetadataCatalogTimerSettings settings) {
		this.settings = settings;
	}

	@Bean(name = "catEventTaskScheduler", destroyMethod = "shutdown")
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		if (settings.getConfig().entrySet().size() == 0) {
			throw new IllegalStateException("No timer based triggers defined");
		}
		threadPoolTaskScheduler.setPoolSize(settings.getConfig().entrySet().size());
		threadPoolTaskScheduler.setThreadNamePrefix("catEventTaskScheduler");
		return threadPoolTaskScheduler;
	}

	@PostConstruct
	@Autowired
	public void setupCatalogEventDispatcher(final ThreadPoolTaskScheduler threadScheduler,
			final MetadataClient metadataClient, final CatalogEventTimerEntryRepository repository,
			final KafkaTemplate<String, CatalogEvent> kafkaTemplate) {
		Set<Entry<String, TimerProperties>> entries = settings.getConfig().entrySet();

		for (Entry<String, TimerProperties> entry : entries) {
			TimerProperties config = entry.getValue();
			CatalogEventDispatcher dispatcher = new CatalogEventDispatcherImpl(metadataClient, repository,
					new KafkaPublisher(kafkaTemplate, config.getTopic()), entry.getKey(), config.getFamily());
			
			CronTrigger cronTrigger = new CronTrigger(config.getCron());
			threadScheduler.schedule(dispatcher, cronTrigger);
		}
	}

}
