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
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class ProductionEventListener implements MqiListener<ProductionEvent> {

	private static final Logger LOGGER = LogManager.getLogger(ProductionEventListener.class);

	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final long pollingIntervalMs;
	private final long pollingInitialDelayMs;
	private final AppStatus appStatus;

	@Autowired
	public ProductionEventListener(final GenericMqiClient mqiClient, final List<MessageFilter> messageFilter,
			@Value("${dissemination-trigger.production-event-listener.polling-interval-ms}") final long pollingIntervalMs,
			@Value("${dissemination-trigger.production-event-listener.polling-initial-delay-ms}") final long pollingInitialDelayMs,
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
			service.execute(new MqiConsumer<ProductionEvent>(mqiClient, ProductCategory.LEVEL_PRODUCTS, this,
					messageFilter, pollingIntervalMs, pollingInitialDelayMs, appStatus));
		}
	}

	@Override
	public MqiMessageEventHandler onMessage(final GenericMessageDto<ProductionEvent> inputMessage)
			throws AbstractCodedException {
		LOGGER.debug("starting conversion of ProductionEvent to DisseminationJob, got message: {}", inputMessage);
		final ProductionEvent productionEvent = inputMessage.getBody();

		final Reporting reporting = ReportingUtils.newReportingBuilder().predecessor(productionEvent.getUid())
				.newReporting("DisseminationTrigger");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(productionEvent.getProductFamily(),
						productionEvent.getKeyObjectStorage()),
				new ReportingMessage("Handling event for %s", productionEvent.getKeyObjectStorage()));

		LOGGER.info("intersects Ocean Mask: {}", productionEvent.getProductName());
		return new MqiMessageEventHandler.Builder<DisseminationJob>(ProductCategory.DISSEMINATION_JOBS)
				.onSuccess(res -> reporting
						.end(new ReportingMessage("End Handling event for %s", productionEvent.getKeyObjectStorage())))
				.onError(e -> reporting.error(new ReportingMessage("Error on handling event for %s: %s",
						productionEvent.getKeyObjectStorage(), LogUtils.toString(e))))
				.publishMessageProducer(() -> {

					if (intersectsOceanMask(productionEvent)) {
						LOGGER.info("skipping job, not intersects Ocean Mask: {}", productionEvent.getProductName());
						return new MqiPublishingJob<>(Collections.emptyList());
					} else {

						final DisseminationJob disseminationJob = new DisseminationJob();
						disseminationJob.setKeyObjectStorage(productionEvent.getKeyObjectStorage());
						disseminationJob.setProductFamily(productionEvent.getProductFamily());
						disseminationJob.setUid(reporting.getUid());

						final GenericPublicationMessageDto<DisseminationJob> outputMessage = new GenericPublicationMessageDto<DisseminationJob>(
								inputMessage.getId(), productionEvent.getProductFamily(), disseminationJob);

						LOGGER.debug("end conversion of ProductionEvent to DisseminationJob, sent message: {}",
								outputMessage);
						return new MqiPublishingJob<DisseminationJob>(Collections.singletonList(outputMessage));
					}
				}).newResult();
	}

	private boolean intersectsOceanMask(ProductionEvent productionEvent) {
		// TODO Auto-generated method stub
		return true;
	}

}
