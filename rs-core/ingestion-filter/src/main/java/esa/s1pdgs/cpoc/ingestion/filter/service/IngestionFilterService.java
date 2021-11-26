package esa.s1pdgs.cpoc.ingestion.filter.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		if (filterProperties != null) {
			// Check if timestamp of product name is in defined reoccuring timespans
			Pattern pattern = Pattern.compile(filterProperties.getNameRegex());
			
			Pattern.matches(filterProperties.getNameRegex(), productName);
			
			Matcher matcher = pattern.matcher(productName);
			if (matcher.find()) {
				String timestampString = matcher.group(filterProperties.getGroupIdx());
				DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date;
	
				try {
					date = format.parse(timestampString);
				} catch (ParseException e) {
					LOG.error("Unable to parse timestampString %s to date object.", timestampString);
					throw e;
				}
	
				messageShouldBeProcessed = filterProperties.getCronDefinition().isSatisfiedBy(date);
			} else {
				// message does not satisfy regex pattern. Ignore for now?
				messageShouldBeProcessed = false;
			}
		}
		
		if (messageShouldBeProcessed) {
			GenericPublicationMessageDto<IngestionJob> result = new GenericPublicationMessageDto<IngestionJob>(
					message.getId(), message.getBody().getProductFamily(), message.getBody());
			results.add(result);
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
