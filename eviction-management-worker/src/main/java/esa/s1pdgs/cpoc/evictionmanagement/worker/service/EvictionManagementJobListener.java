package esa.s1pdgs.cpoc.evictionmanagement.worker.service;

import java.util.Collections;
import java.util.List;
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
import esa.s1pdgs.cpoc.evictionmanagement.worker.config.WorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class EvictionManagementJobListener implements MqiListener<EvictionManagementJob> {

	private static final Logger LOG = LogManager.getLogger(EvictionManagementJobListener.class);
	
	private final AppStatus appStatus;
	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final ObsClient obsClient;
	private final WorkerConfigurationProperties workerConfig;
	
	@Autowired
	public EvictionManagementJobListener(
			final AppStatus appStatus,
			final GenericMqiClient mqiClient,
			final List<MessageFilter> messageFilter,
			final ObsClient obsClient,
			final WorkerConfigurationProperties workerConfig) {
		
		this.appStatus = appStatus;
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.obsClient = obsClient;
		this.workerConfig = workerConfig;
	}
	
	@PostConstruct
	public void initService() {

		if (workerConfig.getPollingIntervalMs() > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(newMqiConsumer());
		}
	}

	@Override
	public MqiMessageEventHandler onMessage(final GenericMessageDto<EvictionManagementJob> inputMessage) {
		
		LOG.debug("starting eviction, got message: {}", inputMessage);
		final EvictionManagementJob evictionJob = inputMessage.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(evictionJob.getUid())
				.newReporting("EvictionManagementWorker");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(evictionJob.getProductFamily(), evictionJob.getKeyObjectStorage()),
				new ReportingMessage("Starting eviction of %s", evictionJob.getKeyObjectStorage())
		);
		
		return new MqiMessageEventHandler.Builder<EvictionEvent>(ProductCategory.EVICTION_EVENTS)
				.onSuccess(res -> reporting.end(
						new ReportingMessage("Eviction of %s was successful", evictionJob.getKeyObjectStorage())
				))
				.onError(e  -> reporting.error(new ReportingMessage(
						"Error evicting %s: %s", evictionJob.getKeyObjectStorage(), LogUtils.toString(e)
				)))
				.publishMessageProducer(() -> {
					evict(evictionJob);
					return createOutputMessage(inputMessage.getId(), evictionJob);
				})
				.newResult();
	}
	
	private MqiConsumer<EvictionManagementJob> newMqiConsumer() {
		return new MqiConsumer<>(
				mqiClient,
				ProductCategory.EVICTION_MANAGEMENT_JOBS,
				this,
				messageFilter,
				workerConfig.getPollingIntervalMs(),
				workerConfig.getPollingInitialDelayMs(),
				appStatus);
	}

	private MqiPublishingJob<EvictionEvent> createOutputMessage(final long inputMessageId,
			final EvictionManagementJob evictionJob) {
		final EvictionEvent evictionEvent = new EvictionEvent();
		evictionEvent.setProductFamily(evictionJob.getProductFamily());
		evictionEvent.setKeyObjectStorage(evictionJob.getKeyObjectStorage());
		final GenericPublicationMessageDto<EvictionEvent> outputMessage = new GenericPublicationMessageDto<EvictionEvent>(
				inputMessageId, evictionJob.getProductFamily(), evictionEvent);
		return new MqiPublishingJob<EvictionEvent>(Collections.singletonList(outputMessage));
	}

	private void evict(EvictionManagementJob evictionJob) throws Exception {
		// TODO Auto-generated method stub

	}

}
