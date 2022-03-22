package esa.s1pdgs.cpoc.ingestion.filter.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.filter.config.IngestionFilterConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.filter.config.IngestionFilterConfigurationProperties.FilterProperties;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.InboxReportingInput;

@Service
public class IngestionFilterService implements MqiListener<IngestionJob> {

	private static final Logger LOG = LogManager.getLogger(IngestionFilterService.class);
	
	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final IngestionFilterConfigurationProperties properties;
	private final AppStatus appStatus;

	@Autowired
	public IngestionFilterService(final GenericMqiClient mqiClient, final List<MessageFilter> messageFilter,
			final AppStatus appStatus, final IngestionFilterConfigurationProperties properties) {
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.properties = properties;
		this.appStatus = appStatus;
	}

	@PostConstruct
	public void initService() {
		if (this.properties.getPollingIntervalMs() > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(newMqiConsumer());
		}
	}

	@Override
	public MqiMessageEventHandler onMessage(GenericMessageDto<IngestionJob> message) throws Exception {
		final IngestionJob ingestionJob = message.getBody();
		
		final String productName;
		if ("auxip".equalsIgnoreCase(ingestionJob.getInboxType())) {
			productName = ingestionJob.getRelativePath();
		} else {
			productName = ingestionJob.getProductName();
		}
		
		MissionId mission = MissionId.valueOf(ingestionJob.getMissionId());
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(mission).predecessor(ingestionJob.getUid())
				.newReporting("IngestionFilter");
		
		LOG.debug("received Ingestion: {}", productName);
		
		reporting.begin(
				new InboxReportingInput(productName, ingestionJob.getRelativePath(), ingestionJob.getPickupBaseURL()),
				new ReportingMessage("Check if product %s whould be processed or ignored", productName));
		
		return new MqiMessageEventHandler.Builder<IngestionJob>(ProductCategory.INGESTION)
				.onSuccess(res -> reporting.end(new ReportingMessage("Finished handling of IngestionJob")))
				.onError(e -> reporting.error(new ReportingMessage(String.format("Error on handling IngestionJob: %s", LogUtils.toString(e)))))
				.publishMessageProducer(() -> handleMessage(message, mission, productName))
				.newResult();
	}
	
	MqiPublishingJob<IngestionJob> handleMessage(GenericMessageDto<IngestionJob> message, MissionId mission,
			String productName) throws ParseException {
		final List<GenericPublicationMessageDto<? extends AbstractMessage>> results = new ArrayList<>();

		FilterProperties filterProperties = properties.getConfig().get(mission);
		
		// When no filter for mission is defined: All messages are allowed
		boolean messageShouldBeProcessed = true;

		Date lastModifiedDate = message.getBody().getLastModified();
		
		if (filterProperties != null) {
			if (lastModifiedDate != null) {
				// Check if the last modification timestamp of the the file is in defined reoccuring timespans
				filterProperties.getCronDefinition().setTimeZone(TimeZone.getTimeZone("UTC"));
				messageShouldBeProcessed = filterProperties.getCronDefinition().isSatisfiedBy(lastModifiedDate);
			} else {
				LOG.warn("message does not have last modification date {} for ", productName);
				messageShouldBeProcessed = false;
			}
		}
		
		if (messageShouldBeProcessed) {
			LOG.debug("message should be processed for {} with last modification date {}", productName, lastModifiedDate);
			GenericPublicationMessageDto<IngestionJob> result = new GenericPublicationMessageDto<IngestionJob>(
					message.getId(), message.getBody().getProductFamily(), message.getBody());
			result.setInputKey(message.getInputKey());
			result.setOutputKey(message.getBody().getProductFamily().toString());
			results.add(result);
		} else {
			LOG.debug("message should be ignored for {} with lastmodification date {}", productName, lastModifiedDate);
		}
		
		return new MqiPublishingJob<>(results);
	}

	private MqiConsumer<IngestionJob> newMqiConsumer() {
		return new MqiConsumer<>(
				mqiClient,
				ProductCategory.INGESTION,
				this,
				messageFilter,
				properties.getPollingIntervalMs(),
				properties.getPollingInitialDelayMs(),
				appStatus);
	}	
}
