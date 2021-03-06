package esa.s1pdgs.cpoc.evictionmanagement.worker.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.evictionmanagement.worker.config.WorkerConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
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
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
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
	private final ErrorRepoAppender errorAppender;
	private final WorkerConfigurationProperties workerConfig;
	
	@Autowired
	public EvictionManagementJobListener(
			final AppStatus appStatus,
			final GenericMqiClient mqiClient,
			final List<MessageFilter> messageFilter,
			final ObsClient obsClient,
			final ErrorRepoAppender errorAppender,
			final WorkerConfigurationProperties workerConfig) {
		
		this.appStatus = appStatus;
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.obsClient = obsClient;
		this.errorAppender = errorAppender;
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
		
		LOG.debug("Starting eviction, got message: {}", inputMessage);
		final EvictionManagementJob evictionJob = inputMessage.getBody();
		
		//FIXME: Extract the mission identifier of CADU sessions #115 
		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.UNDEFINED)
				.predecessor(evictionJob.getUid()).newReporting("EvictionManagementWorker");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(evictionJob.getProductFamily(), evictionJob.getKeyObjectStorage()),
				new ReportingMessage("Starting eviction of %s", evictionJob.getKeyObjectStorage())
		);
		
		return new MqiMessageEventHandler.Builder<EvictionEvent>(ProductCategory.EVICTION_EVENT)
				.onSuccess(res -> reporting.end(
						new ReportingMessage("Eviction of %s was successful", evictionJob.getKeyObjectStorage())
				))
				.onError(e  -> {
					final String errorMessage = String.format("Error evicting %s: %s", evictionJob.getKeyObjectStorage(), LogUtils.toString(e));
					reporting.error(new ReportingMessage(errorMessage));
					LOG.error(errorMessage);
					errorAppender.send(
							new FailedProcessingDto(workerConfig.getHostname(), new Date(), errorMessage, inputMessage)
							);
					}
				)
				.publishMessageProducer(() -> {
					evict(evictionJob);
					return createOutputMessage(inputMessage.getId(), evictionJob, reporting.getUid());
				})
				.newResult();
	}
	
	MqiConsumer<EvictionManagementJob> newMqiConsumer() {
		return new MqiConsumer<>(
				mqiClient,
				ProductCategory.EVICTION_MANAGEMENT_JOBS,
				this,
				messageFilter,
				workerConfig.getPollingIntervalMs(),
				workerConfig.getPollingInitialDelayMs(),
				appStatus);
	}

	MqiPublishingJob<EvictionEvent> createOutputMessage(final long inputMessageId,
			final EvictionManagementJob evictionJob, final UUID reportingUid) {
		final EvictionEvent evictionEvent = new EvictionEvent();
		evictionEvent.setProductFamily(evictionJob.getProductFamily());
		evictionEvent.setKeyObjectStorage(evictionJob.getKeyObjectStorage());
		evictionEvent.setOperatorName(evictionJob.getOperatorName());
		evictionEvent.setUid(reportingUid);
		final GenericPublicationMessageDto<EvictionEvent> outputMessage = new GenericPublicationMessageDto<EvictionEvent>(
				inputMessageId, evictionJob.getProductFamily(), evictionEvent);
		return new MqiPublishingJob<EvictionEvent>(Collections.singletonList(outputMessage));
	}

	private void evict(EvictionManagementJob evictionJob) throws Exception {
		obsClient.delete(new ObsObject(evictionJob.getProductFamily(), evictionJob.getKeyObjectStorage()));
	}

}
