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

package esa.s1pdgs.cpoc.compression.worker.service;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.compression.worker.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.worker.file.FileUploader;
import esa.s1pdgs.cpoc.compression.worker.process.CompressExecutorCallable;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class UncompressProcessor extends AbstractProcessor
		implements Function<CatalogJob, Message<CatalogJob>> {
	private static final Logger LOGGER = LogManager.getLogger(UncompressProcessor.class);
	
	private final CommonConfigurationProperties commonProperties;

	@Autowired
	public UncompressProcessor(final CommonConfigurationProperties commonProperties, final AppStatus appStatus, final CompressionWorkerConfigurationProperties properties,
			final ObsClient obsClient) {
		super(appStatus, properties, obsClient);
		this.commonProperties = commonProperties;
		
		if (properties.isSkipUncompression()) {
			LOGGER.info("Skip uncompression flag is set to true. Just pushing incoming messages forward.");
		}
	}

	@Override
	public Message<CatalogJob> apply(CatalogJob event) {
		CatalogJob result = event;
		
		if (!properties.isSkipUncompression()) {
			result = performUncompression(event);
		}

		return MessageBuilder.withPayload(result).build();
	}
	
	private CatalogJob performUncompression(CatalogJob event) {
		
		if (!event.getProductFamily().isCompressed()) {
			LOGGER.warn("Received uncompressed message (ProductFamily=%s). This may be an error, but still trying to uncompress.");
		}
		
		final String workDir = properties.getWorkingDirectory();

		final MissionId mission = MissionId.fromFileName(event.getKeyObjectStorage());
		
		final Reporting report = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(event.getUid()).newReporting("UncompressionProcessing");

		// Initialize the pool processor executor
		final CompressExecutorCallable procExecutor = new CompressExecutorCallable(mission, event, properties);
		final ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);

		// Initialize the input downloader
		final FileDownloader fileDownloader = new FileDownloader(mission, obsClient, workDir, event);

		ProductFamily outputProductFamily = CompressionEventUtil.removeZipSuffixFromProductFamily(event.getProductFamily());
		
		final FileUploader fileUploader = new FileUploader(mission, obsClient, workDir, event, outputProductFamily);
		report.begin(ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), event.getKeyObjectStorage()),
				new ReportingMessage("Start uncompression processing"));
		
		String keyObs;
		try {

			checkThreadInterrupted();
			LOGGER.info("Downloading inputs for {}", event);
			fileDownloader.processInputs(report);

			checkThreadInterrupted();
			LOGGER.info("Uncompressing inputs for {}", event);
			final Future<?> fut = procCompletionSrv.submit(procExecutor);
			waitForPoolProcessesEnding("Uncompressing inputs for " + event, fut, procCompletionSrv,
					properties.getCompressionTimeout() * 1000L);

			checkThreadInterrupted();
			LOGGER.info("Uploading uncompressed outputs for {}", event);
			keyObs = fileUploader.processOutput(report);

		} catch (AbstractCodedException | InterruptedException | ObsEmptyFileException e) {
			LOGGER.error(e);
			report.error(errorReportMessage(e));
			throw new RuntimeException(e);
		} finally {
			// initially, this has only been performed on InterruptedException but we
			// discussed that it makes sense to
			// always perform the cleanup, also see S1PRO-988
			cleanCompressionProcessing(event, procExecutorSrv);
		}

		report.end(ReportingUtils.newFilenameReportingOutputFor(event.getProductFamily(), event.getKeyObjectStorage()),
				new ReportingMessage("End uncompression processing"));
				
		CatalogJob result = new CatalogJob();
		result.setUid(report.getUid());
		result.setKeyObjectStorage(keyObs);
		result.setStoragePath(obsClient.getAbsoluteStoragePath(outputProductFamily, keyObs));
		result.setMetadataProductName(keyObs);
		result.setProductFamily(outputProductFamily);
		result.setMissionId(event.getMissionId());
		result.setSatelliteId(event.getSatelliteId());
		result.setProductSizeByte(event.getProductSizeByte()); // New size?
		result.setStationName(event.getStationName());
		result.setMetadataMode(event.getMetadataMode());
		result.setTimeliness(event.getTimeliness());
		
		// RS-536: Add RS Chain Version to message
		result.setRsChainVersion(commonProperties.getRsChainVersion());
		
		return result;
	}

}
