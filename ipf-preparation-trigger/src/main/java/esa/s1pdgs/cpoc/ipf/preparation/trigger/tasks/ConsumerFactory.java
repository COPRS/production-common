package esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.AppCatalogConfigurationProperties;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public class ConsumerFactory {	
	private final AppCatalogConfigurationProperties properties;
	private final RestTemplate restTemplate;
	private final ProcessSettings processSettings; 
	private final GenericMqiClient mqiService;
	private final StatusService mqiStatusService;
	private final ErrorRepoAppender errorRepoAppender;
	private final AppStatus appStatus;
	private final MetadataClient metadataClient;
	private final ProductCategory category;
	
	@Value("${process.fixed-delay-ms}") final long pollingIntervalMs,
	 @Value("${process.initial-delay-ms}") final long pollingInitialDelayMs

	public AbstractGenericConsumer<?> newConsumer() {
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
			case LEVEL_SEGMENTS:
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
			case 
			default:
				break;
		}
	}
}
