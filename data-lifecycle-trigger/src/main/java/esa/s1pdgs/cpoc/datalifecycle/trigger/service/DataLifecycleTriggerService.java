package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

@Service
public class DataLifecycleTriggerService {

	private static final Logger LOG = LogManager.getLogger(DataLifecycleTriggerService.class);

	private final DataLifecycleTriggerConfigurationProperties configurationProperties;
	private final MqiClient mqiClient;
	private final AppStatus appStatus;
	private final ErrorRepoAppender errorRepoAppender;
	private final ProcessConfiguration processConfig;

	@Autowired
	public DataLifecycleTriggerService(final DataLifecycleTriggerConfigurationProperties configurationProperties,
			final MqiClient mqiClient, final AppStatus appStatus, final ErrorRepoAppender errorRepoAppender,
			final ProcessConfiguration processConfig) {
		this.configurationProperties = configurationProperties;
		this.mqiClient = mqiClient;
		this.appStatus = appStatus;
		this.errorRepoAppender = errorRepoAppender;
		this.processConfig = processConfig;
	}

	@PostConstruct
	public void initService() {

		CategoryConfig ingestionEventCategoryConfig = configurationProperties.getProductCategories()
				.get(ProductCategory.INGESTION_EVENT);
		MqiConsumer<IngestionEvent> ingestionEventConsumer = new MqiConsumer<IngestionEvent>(mqiClient,
				ProductCategory.INGESTION_EVENT,
				new DataLifecycleTriggerListener<>(mqiClient, errorRepoAppender, processConfig),
				ingestionEventCategoryConfig.getFixedDelayMs(), ingestionEventCategoryConfig.getInitDelayPolMs(),
				appStatus);

		CategoryConfig productionEventCategoryConfig = configurationProperties.getProductCategories()
				.get(ProductCategory.PRODUCTION_EVENT);
		MqiConsumer<ProductionEvent> productionEventConsumer = new MqiConsumer<ProductionEvent>(mqiClient,
				ProductCategory.PRODUCTION_EVENT,
				new DataLifecycleTriggerListener<>(mqiClient, errorRepoAppender, processConfig),
				productionEventCategoryConfig.getFixedDelayMs(), productionEventCategoryConfig.getInitDelayPolMs(),
				appStatus);

		CategoryConfig compressionEventCategoryConfig = configurationProperties.getProductCategories()
				.get(ProductCategory.COMPRESSED_PRODUCTS);
		MqiConsumer<CompressionEvent> compressionEventConsumer = new MqiConsumer<CompressionEvent>(mqiClient,
				ProductCategory.COMPRESSED_PRODUCTS,
				new DataLifecycleTriggerListener<>(mqiClient, errorRepoAppender, processConfig),
				compressionEventCategoryConfig.getFixedDelayMs(), compressionEventCategoryConfig.getInitDelayPolMs(),
				appStatus);

		final ExecutorService executorService = Executors.newFixedThreadPool(3);
		executorService.execute(ingestionEventConsumer);
		executorService.execute(productionEventConsumer);
		executorService.execute(compressionEventConsumer);
	}

}
