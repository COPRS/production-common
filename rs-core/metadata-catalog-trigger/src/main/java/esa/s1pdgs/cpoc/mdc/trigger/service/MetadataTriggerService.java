package esa.s1pdgs.cpoc.mdc.trigger.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdc.trigger.CatalogJobMapper;
import esa.s1pdgs.cpoc.mdc.trigger.MetadataTriggerListener;
import esa.s1pdgs.cpoc.mdc.trigger.config.MdcTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.trigger.config.MdcTriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mdc.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

@Service
public class MetadataTriggerService {	
	private static final CatalogJobMapper<IngestionEvent> INGESTION_MAPPER = new CatalogJobMapper<IngestionEvent>() {
		@Override
		public final CatalogJob toCatJob(final IngestionEvent event, final UUID reportingId) {
			final CatalogJob job = new CatalogJob();
			job.setProductName(event.getProductName());
			job.setRelativePath(event.getRelativePath());
			job.setProductFamily(event.getProductFamily());
			job.setKeyObjectStorage(event.getKeyObjectStorage());
			job.setUid(reportingId);
			job.setStationName(event.getStationName());
			job.setMode(event.getMode());
			job.setTimeliness(event.getTimeliness());
			return job;
		}		
	};
	private static final CatalogJobMapper<ProductionEvent> PROD_MAPPER = new CatalogJobMapper<ProductionEvent>() {
		@Override
		public final CatalogJob toCatJob(final ProductionEvent event, final UUID reportingId) {
			final CatalogJob job = new CatalogJob();
			job.setProductName(event.getProductName());
			// relativ path should not be needed here --> only evaluated for EDRS_SESSION
			job.setProductFamily(event.getProductFamily());
			job.setKeyObjectStorage(event.getKeyObjectStorage());
			job.setMode(event.getMode());
			job.setOqcFlag(event.getOqcFlag());
			job.setUid(reportingId);
			job.setTimeliness(event.getTimeliness());
			return job;
		}		
	};
	
	private static final CatalogJobMapper<CompressionEvent> COMPRESSION_MAPPER = new CatalogJobMapper<CompressionEvent>() {
		@Override
		public final CatalogJob toCatJob(final CompressionEvent event, final UUID reportingId) {
			final CatalogJob job = new CatalogJob();
			job.setProductName(event.getKeyObjectStorage());
			job.setRelativePath(event.getKeyObjectStorage());
			job.setProductFamily(event.getProductFamily());
			job.setKeyObjectStorage(event.getKeyObjectStorage());
			job.setUid(reportingId);
			return job;
		}
	};
	
	private static final Logger LOG = LogManager.getLogger(MetadataTriggerService.class);
	
	private final MdcTriggerConfigurationProperties properties;
	private final MqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final AppStatus appStatus;
	private final ErrorRepoAppender errorAppender;
	private final ProcessConfiguration processConfig;
		
	@Autowired
	public MetadataTriggerService(
			final MdcTriggerConfigurationProperties properties, 
			final MqiClient mqiClient,
			final List<MessageFilter> messageFilter,
			final AppStatus appStatus,
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfig
			
	) {
		this.properties = properties;
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.appStatus = appStatus;
		this.errorAppender = errorAppender;
		this.processConfig = processConfig;
	}

	@PostConstruct
	public void initService() {
		final Map<ProductCategory, CategoryConfig> entries = properties.getProductCategories();		
		final ExecutorService service = Executors.newFixedThreadPool(entries.size());
		
		for (final Map.Entry<ProductCategory, CategoryConfig> entry : entries.entrySet()) {			
			service.execute(newMqiConsumerFor(entry.getKey(), entry.getValue()));
		}
	}
	
	final MqiConsumer<?> newMqiConsumerFor(final ProductCategory cat, final CategoryConfig config) {
		LOG.debug("Creating MQI consumer for category {} using {}", cat, config);
		if (cat == ProductCategory.INGESTION_EVENT) {
			return new MqiConsumer<IngestionEvent>(
					mqiClient, 
					cat, 
					new MetadataTriggerListener<>(INGESTION_MAPPER, errorAppender, processConfig),
					messageFilter,
					config.getFixedDelayMs(),
					config.getInitDelayPolMs(),
					appStatus
			);
		} else if (cat == ProductCategory.PRODUCTION_EVENT) {
			return new MqiConsumer<ProductionEvent>(
					mqiClient, 
					cat, 
					new MetadataTriggerListener<>(PROD_MAPPER, errorAppender, processConfig),
					messageFilter,
					config.getFixedDelayMs(),
					config.getInitDelayPolMs(),
					appStatus
			);
		} else if (cat == ProductCategory.COMPRESSED_PRODUCTS) {
			return new MqiConsumer<CompressionEvent>(
					mqiClient, 
					cat, 
					new MetadataTriggerListener<>(COMPRESSION_MAPPER, errorAppender, processConfig),
					messageFilter,
					config.getFixedDelayMs(),
					config.getInitDelayPolMs(),
					appStatus
			);
		}
		throw new IllegalArgumentException(
				String.format(
						"Invalid product category %s. Available are %s", 
						cat, 
						Arrays.asList(ProductCategory.INGESTION_EVENT, ProductCategory.PRODUCTION_EVENT, ProductCategory.COMPRESSED_PRODUCTS)
				)
		);
	}
}
