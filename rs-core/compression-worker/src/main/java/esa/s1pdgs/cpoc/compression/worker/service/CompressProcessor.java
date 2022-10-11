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
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.compression.worker.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.worker.file.FileUploader;
import esa.s1pdgs.cpoc.compression.worker.process.CompressExecutorCallable;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class CompressProcessor extends AbstractProcessor implements Function<CatalogEvent, Message<CompressionEvent>> {
	private static final Logger LOGGER = LogManager.getLogger(CompressProcessor.class);
	
	private final CommonConfigurationProperties commonProperties;

	@Autowired
	public CompressProcessor(final CommonConfigurationProperties commonProperties, final AppStatus appStatus, final CompressionWorkerConfigurationProperties properties,
			final ObsClient obsClient) {
		super(appStatus, properties, obsClient);
		this.commonProperties = commonProperties;
	}

	@Override
	public final Message<CompressionEvent> apply(final CatalogEvent event) {
		final String workDir = properties.getWorkingDirectory();

		final MissionId mission = MissionId.fromFileName(event.getKeyObjectStorage());
		
		final Reporting report = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(event.getUid()).newReporting("CompressionProcessing");

		// Initialize the pool processor executor
		final CompressExecutorCallable procExecutor = new CompressExecutorCallable(mission, event, properties);
		final ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);

		// Initialize the input downloader
		final FileDownloader fileDownloader = new FileDownloader(mission, obsClient, workDir, event);

		final FileUploader fileUploader = new FileUploader(mission, obsClient, workDir, event,
				CompressionEventUtil.composeCompressedProductFamily(event.getProductFamily()));
		report.begin(ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), event.getKeyObjectStorage()),
				new ReportingMessage("Start compression processing"));

		try {

			checkThreadInterrupted();
			LOGGER.info("Downloading inputs for {}", event);
			fileDownloader.processInputs(report);

			checkThreadInterrupted();
			LOGGER.info("Compressing inputs for {}", event);
			final Future<?> fut = procCompletionSrv.submit(procExecutor);
			waitForPoolProcessesEnding("Compressing inputs for " + event, fut, procCompletionSrv,
					properties.getCompressionTimeout() * 1000L);

			checkThreadInterrupted();
			LOGGER.info("Uploading compressed outputs for {}", event);
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
				new ReportingMessage("End compression processing"));

		CompressionEvent result = new CompressionEvent(
				CompressionEventUtil.composeCompressedProductFamily(event.getProductFamily()),
				CompressionEventUtil.composeCompressedKeyObjectStorage(event.getKeyObjectStorage(), mission));
		result.setMissionId(event.getMissionId());
		result.setSatelliteId(event.getSatelliteId());
		result.setUid(report.getUid());
		result.setStoragePath(obsClient.getAbsoluteStoragePath(event.getProductFamily(), event.getKeyObjectStorage()));

		return MessageBuilder.withPayload(result).build();
	}

}
