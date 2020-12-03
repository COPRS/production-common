package esa.s1pdgs.cpoc.dissemination.trigger.service;

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
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class CompressionEventListener implements MqiListener<CompressionEvent> {

	private static final Logger LOGGER = LogManager.getLogger(CompressionEventListener.class);

	private static final String MANIFEST_SAFE_FILE = "manifest.safe";

	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final MetadataClient metadataClient;
	private final long pollingIntervalMs;
	private final long pollingInitialDelayMs;
	private final AppStatus appStatus;

	@Autowired
	public CompressionEventListener(final GenericMqiClient mqiClient, final List<MessageFilter> messageFilter,
			final MetadataClient metadataClient,
			@Value("${dissemination-trigger.event-listener.polling-interval-ms}") final long pollingIntervalMs,
			@Value("${dissemination-trigger.event-listener.polling-initial-delay-ms}") final long pollingInitialDelayMs,
			final AppStatus appStatus) {
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.metadataClient = metadataClient;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
		this.appStatus = appStatus;
	}

	@PostConstruct
	public void initService() {
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<CompressionEvent>(mqiClient, ProductCategory.LEVEL_PRODUCTS, this,
					messageFilter, pollingIntervalMs, pollingInitialDelayMs, appStatus));
		}
	}

	@Override
	public MqiMessageEventHandler onMessage(final GenericMessageDto<CompressionEvent> inputMessage)
			throws AbstractCodedException {
		LOGGER.debug("starting conversion of CompressionEvent to DisseminationJob, got message: {}", inputMessage);
		final CompressionEvent compressionEvent = inputMessage.getBody();

		final Reporting reporting = ReportingUtils.newReportingBuilder().predecessor(compressionEvent.getUid())
				.newReporting("DisseminationTrigger");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(compressionEvent.getProductFamily(),
						compressionEvent.getKeyObjectStorage()),
				new ReportingMessage("Handling event for %s", compressionEvent.getKeyObjectStorage()));

		final ProductFamily unzippedProductFamily = CompressionEventUtil
				.removeZipSuffixFromProductFamily(compressionEvent.getProductFamily());
		final String unzippedKeyObs = CompressionEventUtil.removeZipSuffix(compressionEvent.getKeyObjectStorage());

		return new MqiMessageEventHandler.Builder<DisseminationJob>(ProductCategory.DISSEMINATION_JOBS)
				.onSuccess(res -> reporting
						.end(new ReportingMessage("End Handling event for %s", compressionEvent.getKeyObjectStorage())))
				.onError(e -> reporting.error(new ReportingMessage("Error on handling event for %s: %s",
						compressionEvent.getKeyObjectStorage(), LogUtils.toString(e))))
				.publishMessageProducer(() -> {

					if (metadataClient.isIntersectingOceanMask(unzippedProductFamily, unzippedKeyObs)) {
						LOGGER.info("intersects ocean mask: {}", compressionEvent.getKeyObjectStorage());
						final DisseminationJob disseminationJob = new DisseminationJob();
						disseminationJob.setKeyObjectStorage(compressionEvent.getKeyObjectStorage());
						disseminationJob.setProductFamily(compressionEvent.getProductFamily());
						disseminationJob.addDisseminationSource(compressionEvent.getProductFamily(),
								compressionEvent.getKeyObjectStorage());
						disseminationJob.addDisseminationSource(unzippedProductFamily,
								unzippedKeyObs + "/" + MANIFEST_SAFE_FILE);
						disseminationJob.setUid(reporting.getUid());

						final GenericPublicationMessageDto<DisseminationJob> outputMessage = new GenericPublicationMessageDto<DisseminationJob>(
								inputMessage.getId(), compressionEvent.getProductFamily(), disseminationJob);

						LOGGER.debug("end conversion of CompressionEvent to DisseminationJob, sent message: {}",
								outputMessage);
						return new MqiPublishingJob<DisseminationJob>(Collections.singletonList(outputMessage));
					} else {
						LOGGER.info("skipping job, product does not intersect ocean mask: {}",
								compressionEvent.getKeyObjectStorage());
						return new MqiPublishingJob<>(Collections.emptyList());
					}
				}).newResult();
	}

}
