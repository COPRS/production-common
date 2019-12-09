package esa.s1pdgs.cpoc.ipf.preparation.trigger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.EdrsSessionConsumer;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.L0SegmentConsumer;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.L0SliceConsumer;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

@Configuration
public class TriggerConfig {


	@SuppressWarnings("unchecked")
	@Bean
	@Autowired
	public AbstractGenericConsumer<? extends AbstractMessage> productMessageConsumer(
			final L0SegmentAppProperties appProperties, final L0SlicePatternSettings patternSettings,
			final ProcessSettings processSettings, final GenericMqiClient mqiService,
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
