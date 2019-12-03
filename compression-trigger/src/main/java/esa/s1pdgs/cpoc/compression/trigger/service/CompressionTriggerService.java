package esa.s1pdgs.cpoc.compression.trigger.service;

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
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.trigger.config.TriggerConfigurationProperties;
import esa.s1pdgs.cpoc.compression.trigger.config.TriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

@Service
public class CompressionTriggerService {

	private static final Logger LOGGER = LogManager.getLogger(CompressionTriggerService.class);
	
	private static final String SUFFIX_ZIPPRODUCTFAMILY = "_ZIP";
	private static final String SUFFIX_ZIPPPRODUCTFILE = ".zip";

	private final GenericMqiClient mqiClient;
	private final TriggerConfigurationProperties properties;
	
	private final AppStatus appStatus;

	@Autowired
	public CompressionTriggerService(final GenericMqiClient mqiClient,
			final AppStatus appStatus,
			final TriggerConfigurationProperties properties) {
		this.mqiClient = mqiClient;
		this.appStatus = appStatus;
		this.properties = properties;
	}
	
	@PostConstruct
	public void initService() {
		LOGGER.info("Setting up product event listeners");
		final Map<ProductCategory, CategoryConfig> entries = properties.getProductCategories();
		final ExecutorService service = Executors.newFixedThreadPool(entries.size());
		
		for (final Map.Entry<ProductCategory, CategoryConfig> entry : entries.entrySet()) {			
			service.execute(newMqiConsumerFor(entry.getKey(), entry.getValue()));
		}
	}
	
	private final MqiConsumer<?> newMqiConsumerFor(final ProductCategory cat, final CategoryConfig config) {
		LOGGER.debug("Creating MQI consumer for category {} using {}", cat, config);
		return new MqiConsumer<ProductionEvent>(
				mqiClient, 
				cat, 
				p -> publish(cat, p, toCompressionJob(p.getBody())),
				config.getFixedDelayMs(),
				config.getInitDelayPolMs(),
				appStatus
		);
	}
	
	final void publish(final ProductCategory cat, final GenericMessageDto<?> mess, final CompressionJob job) {
    	final GenericPublicationMessageDto<CompressionJob> messageDto = new GenericPublicationMessageDto<CompressionJob>(
    			mess.getId(), 
    			job.getProductFamily(), 
    			job
    	);
    	messageDto.setInputKey(mess.getInputKey());
    	messageDto.setOutputKey(job.getOutputProductFamily().name());
		try {
			mqiClient.publish(messageDto, cat);
		} catch (final AbstractCodedException e) {
			throw new RuntimeException(
					String.format("Error publishing %s message %s: %s", cat, messageDto, e.getLogMessage()),
					e
			);
		}
	}
	
	private final CompressionJob toCompressionJob(final ProductionEvent event) {
		return new CompressionJob(
				event.getKeyObjectStorage(), 
				event.getProductFamily(),
				getCompressedKeyObjectStorage(event.getKeyObjectStorage()),
				getCompressedProductFamily(event.getProductFamily()),
				CompressionDirection.COMPRESS
		);
	}
	
	String getCompressedKeyObjectStorage(final String inputKeyObjectStorage) {
		return inputKeyObjectStorage + SUFFIX_ZIPPPRODUCTFILE;
	}

	ProductFamily getCompressedProductFamily(final ProductFamily inputFamily) {
		return ProductFamily.fromValue(inputFamily.toString() + SUFFIX_ZIPPRODUCTFAMILY);
	}

}
