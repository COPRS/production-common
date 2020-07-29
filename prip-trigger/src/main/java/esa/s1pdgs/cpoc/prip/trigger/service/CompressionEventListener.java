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
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.PripPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class CompressionEventListener implements MqiListener<CompressionEvent> {
	
	private static final Logger LOGGER = LogManager.getLogger(CompressionEventListener.class);

	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final long pollingIntervalMs;
	private final long pollingInitialDelayMs;
	private final AppStatus appStatus;

	@Autowired
	public CompressionEventListener(final GenericMqiClient mqiClient,
		    final List<MessageFilter> messageFilter,
			@Value("${prip-trigger.compression-event-listener.polling-interval-ms}") final long pollingIntervalMs,
			@Value("${prip-trigger.compression-event-listener.polling-initial-delay-ms}") final long pollingInitialDelayMs,
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
			service.execute(new MqiConsumer<CompressionEvent>(
					mqiClient, 
					ProductCategory.COMPRESSED_PRODUCTS, 
					this,
					messageFilter,
					pollingIntervalMs, 
					pollingInitialDelayMs, 
					appStatus
			));
		}
	}

	@Override
	public MqiMessageEventHandler onMessage(final GenericMessageDto<CompressionEvent> inputMessage) throws AbstractCodedException {
		LOGGER.debug("starting conversion of CompressionEvent to PublishingJob, got message: {}", inputMessage);		
		final CompressionEvent compressionEvent = inputMessage.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(compressionEvent.getUid())
				.newReporting("PripTrigger");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(compressionEvent.getProductFamily(), compressionEvent.getKeyObjectStorage()),
				new ReportingMessage("Handling event for %s", compressionEvent.getKeyObjectStorage())
		);
		
		return new MqiMessageEventHandler.Builder<PripPublishingJob>(ProductCategory.PRIP_JOBS)
				.onSuccess(res -> reporting.end(new ReportingMessage("End Handling event for %s", compressionEvent.getKeyObjectStorage())))
				.onError(e -> reporting.error(new ReportingMessage(
						"Error on handling event for %s: %s", 
						compressionEvent.getKeyObjectStorage(), 
						LogUtils.toString(e)
				)))
				.messageHandling(() -> {
					final PripPublishingJob publishingJob = new PripPublishingJob();
					publishingJob.setKeyObjectStorage(compressionEvent.getKeyObjectStorage());
					publishingJob.setProductFamily(compressionEvent.getProductFamily());
					publishingJob.setUid(reporting.getUid());
					
					final GenericPublicationMessageDto<PripPublishingJob> outputMessage =
							new GenericPublicationMessageDto<PripPublishingJob>(
									inputMessage.getId(),
									compressionEvent.getProductFamily(), 
									publishingJob
							);

					LOGGER.debug("end conversion of CompressionEvent to PublishingJob, sent message: {}", outputMessage);
		    		return Collections.singletonList(outputMessage);
				})
				.newResult();
	}

}
