package esa.s1pdgs.cpoc.compression.worker.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.compression.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.compression.worker.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.worker.file.FileUploader;
import esa.s1pdgs.cpoc.compression.worker.process.CompressExecutorCallable;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.UnrecoverableErrorAwareObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class CompressProcessor implements Function<CatalogEvent, Message<CompressionEvent>> {
	private static final Logger LOGGER = LogManager.getLogger(CompressProcessor.class);

	private final ApplicationProperties properties;
	private final AppStatus appStatus;
	private final ObsClient obsClient;

	@Autowired
	public CompressProcessor(
			final AppStatus appStatus, 
			final ApplicationProperties properties,
			final ObsClient obsClient
	) {
		this.appStatus = appStatus;
		this.properties = properties;
		this.obsClient = new UnrecoverableErrorAwareObsClient(obsClient, e -> appStatus.getStatus().setFatalError());
	}
	
	@Override
	public final Message<CompressionEvent> apply(final CatalogEvent catalogEvent) {
		final String workDir = properties.getWorkingDirectory();
		
		final Reporting report = ReportingUtils.newReportingBuilder(MissionId.fromFileName(catalogEvent.getKeyObjectStorage()))
				.predecessor(catalogEvent.getUid())				
				.newReporting("CompressionProcessing");
		
		// Initialize the pool processor executor
		final CompressExecutorCallable procExecutor = new CompressExecutorCallable(catalogEvent, properties);
		final ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);

		// Initialize the input downloader
		final FileDownloader fileDownloader = new FileDownloader(
				obsClient, 
				workDir, 
				catalogEvent,
				properties.getSizeBatchDownload()
		);
		final FileUploader fileUploader = new FileUploader(obsClient, workDir, catalogEvent);	
		report.begin(
				ReportingUtils.newFilenameReportingInputFor(catalogEvent.getProductFamily(), catalogEvent.getKeyObjectStorage()),
				new ReportingMessage("Start compression processing")
		);		

		try {

			checkThreadInterrupted();
			LOGGER.info("Downloading inputs for {}", catalogEvent);
			fileDownloader.processInputs(report);

			checkThreadInterrupted();
			LOGGER.info("Compressing inputs for {}", catalogEvent);
			final Future<?> fut = procCompletionSrv.submit(procExecutor);
			waitForPoolProcessesEnding(
					"Compressing inputs for " + catalogEvent,
					fut,
					procCompletionSrv, 
					properties.getTmProcAllTasksS() * 1000L);

			checkThreadInterrupted();
			LOGGER.info("Uploading compressed outputs for {}", catalogEvent);
			fileUploader.processOutput(report);
			
		} catch (AbstractCodedException | InterruptedException | ObsEmptyFileException e) {
			LOGGER.error(e);
			report.error(errorReportMessage(e));
			throw new RuntimeException(e);
		}
		finally {
			// initially, this has only been performed on InterruptedException but we discussed that it makes sense to
			// always perform the cleanup, also see S1PRO-988 
			cleanCompressionProcessing(catalogEvent, procExecutorSrv);
		}
		
		report.end(ReportingUtils.newFilenameReportingOutputFor(catalogEvent.getProductFamily(), catalogEvent.getKeyObjectStorage()), 
			new ReportingMessage("End compression processing"));
		
		CompressionEvent event = new CompressionEvent(
				CompressionEventUtil.composeCompressedProductFamily(catalogEvent.getProductFamily()),
				CompressionEventUtil.composeCompressedKeyObjectStorage(catalogEvent.getKeyObjectStorage()),
				CompressionDirection.COMPRESS);
		
		return MessageBuilder.withPayload(event).build();
	}


	/**
	 * Check if thread interrupted
	 * 
	 * @throws InterruptedException
	 */
	private final void checkThreadInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Current thread is interrupted");
		}
	}

	/**
	 * Wait for the processes execution completion
	 * 
	 * @throws InterruptedException
	 * @throws AbstractCodedException
	 */
	/**
	 * Wait for the processes execution completion
	 */
	// TODO FIXME this needs to be cleaned up to have a self contained cancellation process
	protected void waitForPoolProcessesEnding(
			final String message,
			final Future<?> submittedFuture,
			final ExecutorCompletionService<Void> procCompletionSrv,
			final long timeoutMilliSeconds
	) throws InterruptedException, AbstractCodedException {
		try {
			checkThreadInterrupted();
			final Future<?> future = procCompletionSrv.poll(timeoutMilliSeconds, TimeUnit.MILLISECONDS);
			// timeout scenario
			if (future == null) {
				submittedFuture.cancel(true);
				throw new InterruptedException();
			}	
			if (future.isCancelled()) {
				LOGGER.debug("{}: cancelled", message);
				throw new InterruptedException();
			}		
			future.get();
			LOGGER.debug("{}: successfully executed", message);
		} catch (final ExecutionException e) {
			if (e.getCause() instanceof AbstractCodedException) {
				throw (AbstractCodedException) e.getCause();
			} else {
				throw new InternalErrorException(e.getMessage(), e);
			}
		}
		// timeout scenario: 
		catch (final InterruptedException e) {
			final String errMess = String.format("%s: Timeout after %s seconds",  message, properties.getTmProcAllTasksS());
			
			LOGGER.debug(errMess);
			throw new InternalErrorException(errMess, e);
		}
	}

	private final void cleanCompressionProcessing(final CatalogEvent catalogEvent, final ExecutorService procExecutorSrv) {
		procExecutorSrv.shutdownNow();
		try {
			procExecutorSrv.awaitTermination(properties.getTmProcStopS(), TimeUnit.SECONDS);
			// TODO send kill if fails
		} catch (final InterruptedException e) {
			// Conserves the interruption
			Thread.currentThread().interrupt();
		}
		try {
			LOGGER.info("Erasing local working directory for job {}", catalogEvent);
			
			final Path p = Paths.get(properties.getWorkingDirectory());
			if (p.toFile().exists()) {
				Files.walk(p, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.forEach(File::delete);
			}
		} catch (final IOException e) {
			LOGGER.error("{} [code {}] Failed to erase local working directory", catalogEvent,
					ErrorCode.INTERNAL_ERROR.getCode());
			this.appStatus.setError("PROCESSING");
		}
	}
	
	private final ReportingMessage errorReportMessage(final Exception e) {
		if (e instanceof AbstractCodedException) {
			final AbstractCodedException ace = (AbstractCodedException) e;
			return new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());
		}
		if (e instanceof InterruptedException) {
			return new ReportingMessage("Interrupted job processing");				
		}
		// any other Exception
		return new ReportingMessage("[code {}] {}", ErrorCode.INTERNAL_ERROR, LogUtils.toString(e));
	}
	
}
