package esa.s1pdgs.cpoc.datarequest.worker.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.datarequest.worker.config.WorkerConfigurationProperties;
import esa.s1pdgs.cpoc.datarequest.worker.report.DataRequestReportingOutput;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestType;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class DataRequestJobListener implements MqiListener<DataRequestJob> {
	
	private static final Logger LOG = LogManager.getLogger(DataRequestJobListener.class);
	
	private final AppStatus appStatus;
	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final ObsClient obsClient;
	private final ErrorRepoAppender errorAppender;
	private final WorkerConfigurationProperties workerConfig;
	
	@Autowired
	public DataRequestJobListener(
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
	public MqiMessageEventHandler onMessage(final GenericMessageDto<DataRequestJob> inputMessage) {
		LOG.debug("Starting data request, got message: {}", inputMessage);
		final DataRequestJob dataRequestJob = inputMessage.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(dataRequestJob.getUid())
				.newReporting("DataRequestWorker");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(dataRequestJob.getProductFamily(), dataRequestJob.getKeyObjectStorage()),
				new ReportingMessage("Starting data request of %s", dataRequestJob.getKeyObjectStorage())
		);
		
		return new MqiMessageEventHandler.Builder<DataRequestEvent>(ProductCategory.EVICTION_EVENTS)
				.onSuccess(res -> {
					DataRequestType type = res.get(0).getDto().getDataRequestType();
					reporting.end(
							new DataRequestReportingOutput(type.toString()),
							new ReportingMessage(
									"Data request of %s with request type %S was successfull",
									dataRequestJob.getKeyObjectStorage(), type.toString())
							);
					}
				)
				.onError(e  -> {
					final String errorMessage = String.format("Error requesting data of %s: %s", dataRequestJob.getKeyObjectStorage(), LogUtils.toString(e));
					reporting.error(new ReportingMessage(errorMessage));
					LOG.error(errorMessage);
					errorAppender.send(
							new FailedProcessingDto(workerConfig.getHostname(), new Date(), errorMessage, inputMessage)
							);
					}
				)
				.publishMessageProducer(() -> {
					DataRequestType dataRequestType;
					if(checkAvailableInZipBucket(dataRequestJob)) {
						LOG.info("File is available in ZIP bucket: {}, requesting uncompression", dataRequestJob.getKeyObjectStorage());
						dataRequestType = DataRequestType.UNCOMPRESS;
					} else {
						LOG.info("File is missing in ZIP bucket: {}, requesting order", dataRequestJob.getKeyObjectStorage());
						dataRequestType = DataRequestType.ORDER;
					}
					return createOutputMessage(inputMessage.getId(), dataRequestJob, dataRequestType);
				})
				.newResult();
	}
	
	private MqiConsumer<DataRequestJob> newMqiConsumer() {
		return new MqiConsumer<>(
				mqiClient,
				ProductCategory.DATA_REQUEST_JOBS,
				this,
				messageFilter,
				workerConfig.getPollingIntervalMs(),
				workerConfig.getPollingInitialDelayMs(),
				appStatus);
	}
	
	private MqiPublishingJob<DataRequestEvent> createOutputMessage(final long inputMessageId,
			final DataRequestJob dataRequestJob, final DataRequestType dataRequestType) {
		
		final DataRequestEvent dataRequestEvent = new DataRequestEvent();
		dataRequestEvent.setProductFamily(dataRequestJob.getProductFamily());
		dataRequestEvent.setKeyObjectStorage(dataRequestJob.getKeyObjectStorage());
		dataRequestEvent.setDataRequestType(dataRequestType);
		final GenericPublicationMessageDto<DataRequestEvent> outputMessage = new GenericPublicationMessageDto<DataRequestEvent>(
				inputMessageId, dataRequestJob.getProductFamily(), dataRequestEvent);
		return new MqiPublishingJob<DataRequestEvent>(Collections.singletonList(outputMessage));
	}

	private boolean checkAvailableInZipBucket(final DataRequestJob dataRequestJob) throws Exception {
		return obsClient.exists(
				new ObsObject(
						CompressionEventUtil.composeCompressedProductFamily(dataRequestJob.getProductFamily()),
						CompressionEventUtil.composeCompressedKeyObjectStorage(dataRequestJob.getKeyObjectStorage())));
	}

}
