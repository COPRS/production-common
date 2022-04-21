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
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.compression.worker.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.worker.file.FileUploader;
import esa.s1pdgs.cpoc.compression.worker.process.CompressExecutorCallable;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class UncompressProcessor extends AbstractProcessor
		implements Function<IngestionEvent, Message<IngestionEvent>> {
	private static final Logger LOGGER = LogManager.getLogger(UncompressProcessor.class);

	@Autowired
	public UncompressProcessor(final AppStatus appStatus, final CompressionWorkerConfigurationProperties properties,
			final ObsClient obsClient) {
		super(appStatus, properties, obsClient);
	}

	@Override
	public Message<IngestionEvent> apply(IngestionEvent event) {
		// TODO Auto-generated method stub
		final String workDir = properties.getWorkingDirectory();

		final Reporting report = ReportingUtils.newReportingBuilder(MissionId.fromFileName(event.getKeyObjectStorage()))
				.predecessor(event.getUid()).newReporting("UncompressionProcessing");

		// Initialize the pool processor executor
		final CompressExecutorCallable procExecutor = new CompressExecutorCallable(event, properties);
		final ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);

		// Initialize the input downloader
		final FileDownloader fileDownloader = new FileDownloader(obsClient, workDir, event);

		ProductFamily outputProductFamily = CompressionEventUtil.removeZipSuffixFromProductFamily(event.getProductFamily());
		
		final FileUploader fileUploader = new FileUploader(obsClient, workDir, event, outputProductFamily);
		report.begin(ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), event.getKeyObjectStorage()),
				new ReportingMessage("Start uncompression processing"));

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
			fileUploader.processOutput(report);

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

		IngestionEvent result = new IngestionEvent();
		result.setKeyObjectStorage(CompressionEventUtil.removeZipFromKeyObjectStorage(event.getKeyObjectStorage()));
		result.setProductName(workDir);
		result.setProductFamily(outputProductFamily);
		result.setRelativePath(event.getRelativePath());
		result.setMissionId(event.getMissionId());
		result.setProductSizeByte(event.getProductSizeByte()); // New size?
		result.setStationName(event.getStationName());
		result.setMode(event.getMode());
		result.setTimeliness(event.getTimeliness());


		return MessageBuilder.withPayload(result).build();
	}

}
