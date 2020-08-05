package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

@Service
public class DataLifecycleTriggerService {
	private static final List<ProductCategory> CATEGORIES = Arrays.asList(
			ProductCategory.INGESTION_EVENT,
			ProductCategory.PRODUCTION_EVENT,
			ProductCategory.COMPRESSED_PRODUCTS,
			ProductCategory.LTA_DOWNLOAD_EVENT
	);
	
	private final DataLifecycleTriggerConfigurationProperties configurationProperties;
	private final MqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final AppStatus appStatus;
	private final ErrorRepoAppender errorRepoAppender;
	private final ProcessConfiguration processConfig;

	@Autowired
	public DataLifecycleTriggerService(
			final DataLifecycleTriggerConfigurationProperties configurationProperties,
			final MqiClient mqiClient, 
			final List<MessageFilter> messageFilter,
			final AppStatus appStatus, 
			final ErrorRepoAppender errorRepoAppender,
			final ProcessConfiguration processConfig
	) {
		this.configurationProperties = configurationProperties;
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.appStatus = appStatus;
		this.errorRepoAppender = errorRepoAppender;
		this.processConfig = processConfig;
	}
	
	@PostConstruct
	public void initService() {		
		final ExecutorService executorService = Executors.newFixedThreadPool(CATEGORIES.size());
		
		for (final ProductCategory cat : CATEGORIES) {
			executorService.execute(newConsumerFor(cat));
		}
	}

	private final <E extends AbstractMessage> MqiConsumer<E> newConsumerFor(final ProductCategory cat) {
		final CategoryConfig conf = configurationProperties.getProductCategories().get(cat);
		final DataLifecycleTriggerListener<E> listener = new DataLifecycleTriggerListener<>(
				errorRepoAppender, 
				processConfig,
				configurationProperties.getRetentionPolicies()
		);
		return new MqiConsumer<E>(
				mqiClient,
				cat,
				listener,
				messageFilter,
				conf.getFixedDelayMs(), 
				conf.getInitDelayPolMs(),
				appStatus
		);
	}
}
