package esa.s1pdgs.cpoc.prip.trigger.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.PripPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class IngestionEventListener implements MqiListener<IngestionEvent> {
	
	private static final Logger LOGGER = LogManager.getLogger(IngestionEventListener.class);

	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final long pollingIntervalMs;
	private final long pollingInitialDelayMs;
	private final AppStatus appStatus;

	@Autowired
	public IngestionEventListener(final GenericMqiClient mqiClient,
		    final List<MessageFilter> messageFilter,
			@Value("${prip-trigger.ingestion-event-listener.polling-interval-ms}") final long pollingIntervalMs,
			@Value("${prip-trigger.ingestion-event-listener.polling-initial-delay-ms}") final long pollingInitialDelayMs,
			final AppStatus appStatus) {
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
		this.appStatus = appStatus;
	}
	
	@PostConstruct
	public void initService() {
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<IngestionEvent>(
					mqiClient, 
					ProductCategory.INGESTION_EVENT, 
					this,
					messageFilter,
					pollingIntervalMs, 
					pollingInitialDelayMs, 
					appStatus
			));
		}
	}

	@Override
	public MqiMessageEventHandler onMessage(final GenericMessageDto<IngestionEvent> inputMessage) throws AbstractCodedException {
		LOGGER.debug("starting conversion of IngestionEvent to PublishingJob, got message: {}", inputMessage);		
		final IngestionEvent ingestionEvent = inputMessage.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(ingestionEvent.getUid())
				.newReporting("PripTrigger");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(ingestionEvent.getProductFamily(), ingestionEvent.getKeyObjectStorage()),
				new ReportingMessage("Handling event for %s", ingestionEvent.getKeyObjectStorage())
		);
		
		return new MqiMessageEventHandler.Builder<PripPublishingJob>(ProductCategory.PRIP_JOBS)
				.onSuccess(res -> reporting.end(new ReportingMessage("End Handling event for %s", ingestionEvent.getKeyObjectStorage())))
				.onError(e -> reporting.error(new ReportingMessage(
						"Error on handling event for %s: %s", 
						ingestionEvent.getKeyObjectStorage(), 
						LogUtils.toString(e)
				)))
				.publishMessageProducer(() -> {
					final PripPublishingJob publishingJob = new PripPublishingJob();
					publishingJob.setKeyObjectStorage(ingestionEvent.getKeyObjectStorage());
					publishingJob.setProductFamily(ingestionEvent.getProductFamily());
					publishingJob.setUid(reporting.getUid());
					
					final GenericPublicationMessageDto<PripPublishingJob> outputMessage =
							new GenericPublicationMessageDto<PripPublishingJob>(
									inputMessage.getId(),
									ingestionEvent.getProductFamily(), 
									publishingJob
							);

					LOGGER.debug("end conversion of IngestionEvent to PublishingJob, sent message: {}", outputMessage);
		    		return new MqiPublishingJob<PripPublishingJob>(Collections.singletonList(outputMessage));
				})
				.newResult();
	}

}
