/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.datarequest.worker.service;

import java.util.Collections;
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
import esa.s1pdgs.cpoc.datarequest.worker.config.WorkerConfigurationProperties;
import esa.s1pdgs.cpoc.datarequest.worker.report.DataRequestReportingOutput;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
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

@Service
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
		
		//FIXME: Extract the mission identifier of CADU sessions #115 
		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.UNDEFINED)
				.predecessor(dataRequestJob.getUid()).newReporting("DataRequestWorker");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(dataRequestJob.getProductFamily(), dataRequestJob.getKeyObjectStorage()),
				new ReportingMessage("Starting data request of %s", dataRequestJob.getKeyObjectStorage())
		);
		
		return new MqiMessageEventHandler.Builder<DataRequestEvent>(ProductCategory.DATA_REQUEST_EVENT)
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
// NOTE: FailedProcessing is no longer compatible
//					errorAppender.send(
//							new FailedProcessing(workerConfig.getHostname(), new Date(), errorMessage, inputMessage)
//							);
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
					return createOutputMessage(inputMessage.getId(), dataRequestJob, reporting.getUid(), dataRequestType);
				})
				.newResult();
	}
	
	MqiConsumer<DataRequestJob> newMqiConsumer() {
		return new MqiConsumer<>(
				mqiClient,
				ProductCategory.DATA_REQUEST_JOBS,
				this,
				messageFilter,
				workerConfig.getPollingIntervalMs(),
				workerConfig.getPollingInitialDelayMs(),
				appStatus);
	}
	
	MqiPublishingJob<DataRequestEvent> createOutputMessage(final long inputMessageId,
			final DataRequestJob dataRequestJob, final UUID reportingUid, final DataRequestType dataRequestType) {
		
		final DataRequestEvent dataRequestEvent = new DataRequestEvent();
		dataRequestEvent.setProductFamily(dataRequestJob.getProductFamily());
		dataRequestEvent.setKeyObjectStorage(dataRequestJob.getKeyObjectStorage());
		dataRequestEvent.setDataRequestType(dataRequestType);
		dataRequestEvent.setOperatorName(dataRequestJob.getOperatorName());
		dataRequestEvent.setUid(reportingUid);
		final GenericPublicationMessageDto<DataRequestEvent> outputMessage = new GenericPublicationMessageDto<DataRequestEvent>(
				inputMessageId, dataRequestJob.getProductFamily(), dataRequestEvent);
		return new MqiPublishingJob<DataRequestEvent>(Collections.singletonList(outputMessage));
	}

	boolean checkAvailableInZipBucket(final DataRequestJob dataRequestJob) throws Exception {
		return obsClient.exists(
				new ObsObject(
						CompressionEventUtil.composeCompressedProductFamily(dataRequestJob.getProductFamily()),
						CompressionEventUtil.composeCompressedKeyObjectStorage(dataRequestJob.getKeyObjectStorage())));
	}

}
