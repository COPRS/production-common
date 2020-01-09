package esa.s1pdgs.cpoc.production.trigger.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.production.trigger.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.production.trigger.tasks.EdrsSessionConsumer;
import esa.s1pdgs.cpoc.production.trigger.tasks.L0SegmentConsumer;
import esa.s1pdgs.cpoc.production.trigger.tasks.L0SliceConsumer;

@Configuration
public class TriggerConfig {	
	private final AppCatalogConfigurationProperties properties;
	private final RestTemplate restTemplate;
	private final ProcessSettings processSettings; 
	private final GenericMqiClient mqiService;
	private final StatusService mqiStatusService;
	private final ErrorRepoAppender errorRepoAppender;
	private final AppStatus appStatus;
	private final MetadataClient metadataClient;
	
	@Autowired
	public TriggerConfig(
			final AppCatalogConfigurationProperties properties,
			final RestTemplateBuilder restTemplateBuilder,
			final ProcessSettings processSettings, 
			final GenericMqiClient mqiService, 
			final StatusService mqiStatusService,
			final ErrorRepoAppender errorRepoAppender, 
			final AppStatus appStatus,
			final MetadataClient metadataClient
	) {
		this.processSettings = processSettings;
		this.mqiService = mqiService;
		this.mqiStatusService = mqiStatusService;
		this.errorRepoAppender = errorRepoAppender;
		this.appStatus = appStatus;
		this.metadataClient = metadataClient;
		this.properties = properties;
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(properties.getTmConnectMs())
				.build();
	}

	@Bean
	public AbstractGenericConsumer<?> newConsumer() {
		final AppCatalogJobClient<CatalogEvent> appCatClient = new AppCatalogJobClient<>(
				restTemplate, 
				properties.getHostUri(), 
				properties.getMaxRetries(), 
				properties.getTempoRetryMs(), 
				ProductCategory.CATALOG_EVENT
		);
		
		switch (processSettings.getCategory()) {
			case EDRS_SESSIONS:
				return new EdrsSessionConsumer(
						processSettings, 
						mqiService, 
						mqiStatusService, 
						appCatClient,
						errorRepoAppender, 
						appStatus, 
						metadataClient
				);
			case LEVEL_SEGMENTS:
				return new L0SegmentConsumer(
						processSettings, 
						mqiService, 
						mqiStatusService, 
						appCatClient,
						errorRepoAppender, 
						appStatus, 
						metadataClient
				);
			case LEVEL_PRODUCTS:
				return new L0SliceConsumer(
						processSettings, 
						mqiService, 
						mqiStatusService, 
						appCatClient,
						errorRepoAppender, 
						appStatus,
						metadataClient
				);
			default:
				throw new IllegalStateException(
						String.format(
								"Invalid category %s, configured are: %s", 
								processSettings.getCategory(),
								Arrays.asList(
										ProductCategory.EDRS_SESSIONS, 
										ProductCategory.LEVEL_SEGMENTS,
										ProductCategory.LEVEL_PRODUCTS
								)
						)
				);
		}
	}
}
