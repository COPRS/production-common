package esa.s1pdgs.cpoc.ipf.preparation.trigger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.EdrsSessionConsumer;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.L0SegmentConsumer;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.L0SliceConsumer;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

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
	private final ProductCategory category;
	
	@Autowired
	public TriggerConfig(
			final AppCatalogConfigurationProperties properties,
			final RestTemplateBuilder restTemplateBuilder,
			final ProcessSettings processSettings, 
			final GenericMqiClient mqiService, 
			final StatusService mqiStatusService,
			final ErrorRepoAppender errorRepoAppender, 
			final AppStatus appStatus,
			final MetadataClient metadataClient,
			final ProductCategory category
	) {
		this.processSettings = processSettings;
		this.mqiService = mqiService;
		this.mqiStatusService = mqiStatusService;
		this.errorRepoAppender = errorRepoAppender;
		this.appStatus = appStatus;
		this.metadataClient = metadataClient;
		this.category = category;
		this.properties = properties;
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(properties.getTmConnectMs())
				.build();
	}


	@Bean
	public AbstractGenericConsumer<? extends AbstractMessage> newConsumer() {
		
		final AppCatalogJobClient<CatalogEvent> appCatClient = new AppCatalogJobClient<>(
				restTemplate, 
				properties.getHostUri(), 
				properties.getMaxRetries(), 
				properties.getTempoRetryMs(), 
				category
		);		
		switch (category) {
			case EDRS_SESSIONS:
				return new EdrsSessionConsumer(
						processSettings, 
						mqiService, 
						mqiStatusService, 
						appCatClient,
						errorRepoAppender, 
						appStatus, 
						metadataClient, 
						processSettings.getFixedDelayMs(), 
						processSettings.getInitialDelayMs()
				);
			case EDRS_SESSIONS:
				return new L0SegmentConsumer(
						processSettings, 
						mqiService, 
						mqiStatusService, 
						appCatClient,
						errorRepoAppender, 
						appStatus, 
						metadataClient, 
						processSettings.getFixedDelayMs(), 
						processSettings.getInitialDelayMs()
				);
			default:
				break;
		}
		
	}

	@SuppressWarnings("unchecked")
	@Bean
	@Autowired
	public AbstractGenericConsumer<? extends AbstractMessage> productMessageConsumer(
			final ProcessSettings processSettings, 
			final GenericMqiClient mqiService,
			final StatusService mqiStatusService,
			@Qualifier("appCatalogServiceForLevelProducts") final AppCatalogJobClient appDataServiceLevelProducts,
			@Qualifier("appCatalogServiceForEdrsSessions") final AppCatalogJobClient appDataServiceErdsSettions,
			@Qualifier("appCatalogServiceForLevelSegments") final AppCatalogJobClient appDataServiceLevelSegments,
			final ErrorRepoAppender errorRepoAppender, final AppStatus appStatus, final MetadataClient metadataClient,
			 @Value("${process.fixed-delay-ms}") final long pollingIntervalMs,
			 @Value("${process.initial-delay-ms}") final long pollingInitialDelayMs) {

		AbstractGenericConsumer<? extends AbstractMessage> messageConsumer;

		switch (processSettings.getLevel()) {
		case L0:
			messageConsumer = new EdrsSessionConsumer(
					processSettings, mqiService, mqiStatusService, appDataServiceErdsSettions,
					errorRepoAppender, appStatus, metadataClient, pollingIntervalMs, pollingInitialDelayMs);
			break;
		case L0_SEGMENT:
			messageConsumer = new L0SegmentConsumer(
					appProperties, processSettings, mqiService, mqiStatusService, appDataServiceLevelSegments,
					errorRepoAppender, appStatus, pollingIntervalMs, pollingInitialDelayMs);
			break;
		case L1:
		case L2:
			messageConsumer = new L0SliceConsumer(
					patternSettings, processSettings, mqiService, mqiStatusService, appDataServiceLevelProducts,
					errorRepoAppender, appStatus, metadataClient, pollingIntervalMs, pollingInitialDelayMs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported Application Level");
		}
		messageConsumer.setTaskForFunctionalLog(String.format("%sJobGeneration", processSettings.getLevel().name()));
		return messageConsumer;
	}
}
