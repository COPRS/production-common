package esa.s1pdgs.cpoc.compression.trigger.service;

import java.util.List;
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
	
	private static final CompressionJobMapper<ProductionEvent> PROD_MAPPER = (event, reportingId) -> {
		CompressionDirection compressionDirection;

		if (event.getProductFamily().toString().endsWith(SUFFIX_ZIPPRODUCTFAMILY)) {
			compressionDirection = CompressionDirection.UNDEFINED;
		} else {
			compressionDirection = CompressionDirection.COMPRESS;
		}

		return new CompressionJob(event.getKeyObjectStorage(), event.getProductFamily(),
				getCompressedKeyObjectStorage(event.getKeyObjectStorage()),
				getCompressedProductFamily(event.getProductFamily()), compressionDirection);

	};
	
	private static final CompressionJobMapper<IngestionEvent> INGESTION_MAPPER = (event, reportingId) -> {
		CompressionDirection compressionDirection;

		if (event.getProductFamily().toString().endsWith(SUFFIX_ZIPPRODUCTFAMILY)) {
			compressionDirection = CompressionDirection.UNCOMPRESS;
		} else {
			compressionDirection = CompressionDirection.UNDEFINED;
		}

		return new CompressionJob(event.getKeyObjectStorage(), event.getProductFamily(),
				removeZipSuffix(event.getKeyObjectStorage()),
				ProductFamily.fromValue(removeZipSuffix(event.getProductFamily().toString())), compressionDirection);
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
	
	private MqiConsumer<?> newMqiConsumerFor(final ProductCategory cat, final CategoryConfig config) {
		LOGGER.debug("Creating MQI consumer for category {} using {}", cat, config);

		if (ProductionEvent.class.isAssignableFrom(cat.getDtoClass())) {
			return new MqiConsumer<>(
					mqiClient,
					cat,
					new CompressionTriggerListener<>(PROD_MAPPER, errorAppender, processConfig),
					messageFilter,
					config.getFixedDelayMs(),
					config.getInitDelayPolMs(),
					appStatus
			);
		} else if (IngestionEvent.class.isAssignableFrom(cat.getDtoClass())) {
			return new MqiConsumer<>(
					mqiClient,
					cat,
					new CompressionTriggerListener<>(INGESTION_MAPPER, errorAppender, processConfig),
					messageFilter,
					config.getFixedDelayMs(),
					config.getInitDelayPolMs(),
					appStatus
			);
			
		}
		throw new IllegalArgumentException(
				String.format(
						"Invalid product category %s. Available are categories with associated dtos of type ProductionEvent or IngestionEvent",
						cat
				)
		);
	}

	static String getCompressedKeyObjectStorage(final String inputKeyObjectStorage) {
		return inputKeyObjectStorage + SUFFIX_ZIPPPRODUCTFILE;
	}

	static ProductFamily getCompressedProductFamily(final ProductFamily inputFamily) {
		return ProductFamily.fromValue(inputFamily.toString() + SUFFIX_ZIPPRODUCTFAMILY);
	}
	
	static String removeZipSuffix(final String name) {
		if (name.toLowerCase().endsWith(".zip")) {
			return name.substring(0, name.length() - ".zip".length());
		} else if (name.toLowerCase().endsWith("_zip")) {
			return name.substring(0, name.length() - "_zip".length());
		}
		return name;
	}
}
