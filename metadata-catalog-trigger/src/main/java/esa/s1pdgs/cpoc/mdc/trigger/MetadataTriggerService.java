package esa.s1pdgs.cpoc.mdc.trigger;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mdc.trigger.config.MdcTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.trigger.config.MdcTriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

@Service
public class MetadataTriggerService {	
	private static final Logger LOG = LogManager.getLogger(MetadataTriggerService.class);
	
	private final MdcTriggerConfigurationProperties properties;
	private final MqiClient mqiClient;
	private final AppStatus appStatus;
		
	@Autowired
	public MetadataTriggerService(
			final MdcTriggerConfigurationProperties properties, 
			final MqiClient mqiClient,
			final AppStatus appStatus
	) {
		this.properties = properties;
		this.mqiClient = mqiClient;
		this.appStatus = appStatus;
	}

	@PostConstruct
	public void initService() {
		final Map<ProductCategory, CategoryConfig> entries = properties.getProductCategories();		
		final ExecutorService service = Executors.newFixedThreadPool(entries.size());
		
		for (final Map.Entry<ProductCategory, CategoryConfig> entry : entries.entrySet()) {			
			service.execute(newMqiConsumerFor(entry.getKey(), entry.getValue()));
		}
	}
	
	final void publish(final ProductCategory cat, final GenericMessageDto<?> mess, final CatalogJob job) {
    	final GenericPublicationMessageDto<CatalogJob> messageDto = new GenericPublicationMessageDto<CatalogJob>(
    			mess.getId(), 
    			job.getProductFamily(), 
    			job
    	);
    	messageDto.setInputKey(mess.getInputKey());
    	messageDto.setOutputKey(job.getProductFamily().name());
		try {
			mqiClient.publish(messageDto, cat);
		} catch (final AbstractCodedException e) {
			throw new RuntimeException(
					String.format("Error publishing %s message %s: %s", cat, messageDto, e.getLogMessage()),
					e
			);
		}
	}
	
	private final MqiConsumer<?> newMqiConsumerFor(final ProductCategory cat, final CategoryConfig config) {
		LOG.debug("Creating MQI consumer for category {} using {}", cat, config);
		return new MqiConsumer<IngestionEvent>(
				mqiClient, 
				cat, 
				p -> publish(cat, p, toCatalogJob(p.getBody())),
				config.getFixedDelayMs(),
				config.getInitDelayPolMs(),
				appStatus
		);
	}
		
	private final CatalogJob toCatalogJob(final IngestionEvent event) {
		final CatalogJob job = new CatalogJob();
		job.setProductName(event.getProductName());
		job.setRelativePath(event.getRelativePath());
		job.setProductFamily(event.getProductFamily());
		job.setKeyObjectStorage(event.getKeyObjectStorage());
		return job;
	}
}
