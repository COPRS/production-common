package esa.s1pdgs.cpoc.compression.trigger.service;

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
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.compression.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.compression.trigger.config.TriggerConfigurationProperties;
import esa.s1pdgs.cpoc.compression.trigger.config.TriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

@Service
public class CompressionTriggerService {

	private static final Logger LOGGER = LogManager.getLogger(CompressionTriggerService.class);
	private static final String SUFFIX_ZIPPRODUCTFAMILY = "_ZIP";
	private static final String SUFFIX_ZIPPPRODUCTFILE = ".zip";
	
	private static final CompressionJobMapper<ProductionEvent> PROD_MAPPER = new CompressionJobMapper<ProductionEvent>() {
		@Override
		public final CompressionJob toCompressionJob(final ProductionEvent event, final UUID reportingId) {
			CompressionDirection compressionDirection;

			if (event.getProductFamily().toString().endsWith(SUFFIX_ZIPPRODUCTFAMILY)) {
				compressionDirection = CompressionDirection.UNDEFINED;
			} else {
				compressionDirection = CompressionDirection.COMPRESS;
			}

			return new CompressionJob(event.getKeyObjectStorage(), event.getProductFamily(),
					getCompressedKeyObjectStorage(event.getKeyObjectStorage()),
					getCompressedProductFamily(event.getProductFamily()), compressionDirection);
			
		}		
	};
	
	private static final CompressionJobMapper<IngestionEvent> INGESTION_MAPPER = new CompressionJobMapper<IngestionEvent>() {
		@Override
		public final CompressionJob toCompressionJob(final IngestionEvent event, final UUID reportingId) {
			CompressionDirection compressionDirection;

			if (event.getProductFamily().toString().endsWith(SUFFIX_ZIPPRODUCTFAMILY)) {
				compressionDirection = CompressionDirection.UNCOMPRESS;
			} else {
				compressionDirection = CompressionDirection.UNDEFINED;
			}

			return new CompressionJob(event.getKeyObjectStorage(), event.getProductFamily(),
					getCompressedKeyObjectStorage(event.getKeyObjectStorage()),
					getCompressedProductFamily(event.getProductFamily()), compressionDirection);
		}		
	};

	private final MqiClient mqiClient;
	private final TriggerConfigurationProperties properties;
	private final List<MessageFilter> messageFilter;
	private final ErrorRepoAppender errorAppender;
	private final ProcessConfiguration processConfig;
	private final AppStatus appStatus;
	

	@Autowired
	public CompressionTriggerService(
			final MqiClient mqiClient,
			final List<MessageFilter> messageFilter,
			final AppStatus appStatus,
			final TriggerConfigurationProperties properties,
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfig
	) {
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.appStatus = appStatus;
		this.properties = properties;
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
	
	private final MqiConsumer<?> newMqiConsumerFor(final ProductCategory cat, final CategoryConfig config) {
		LOGGER.debug("Creating MQI consumer for category {} using {}", cat, config);
		
		if (cat == ProductCategory.LEVEL_SEGMENTS || cat == ProductCategory.LEVEL_PRODUCTS) {
			return new MqiConsumer<ProductionEvent>(
					mqiClient, 
					cat, 
					new CompressionTriggerListener(PROD_MAPPER, errorAppender, processConfig),
					messageFilter,
					config.getFixedDelayMs(),
					config.getInitDelayPolMs(),
					appStatus
			);
		} else if (cat == ProductCategory.INGESTION_EVENT) {
			return new MqiConsumer<IngestionEvent>(
					mqiClient, 
					cat, 
					new CompressionTriggerListener(INGESTION_MAPPER, errorAppender, processConfig),
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
						Arrays.asList(ProductCategory.LEVEL_PRODUCTS, ProductCategory.LEVEL_SEGMENTS, ProductCategory.INGESTION_EVENT)
				)
		);
	}

	static String getCompressedKeyObjectStorage(final String inputKeyObjectStorage) {
		return inputKeyObjectStorage + SUFFIX_ZIPPPRODUCTFILE;
	}

	static ProductFamily getCompressedProductFamily(final ProductFamily inputFamily) {
		return ProductFamily.fromValue(inputFamily.toString() + SUFFIX_ZIPPRODUCTFAMILY);
	}
}
