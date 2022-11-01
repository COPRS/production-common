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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.UnrecoverableErrorAwareObsClient;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public abstract class AbstractProcessor {
	private static final Logger LOGGER = LogManager.getLogger(AbstractProcessor.class);
	
	protected final CompressionWorkerConfigurationProperties properties;
	protected final AppStatus appStatus;
	protected final ObsClient obsClient;

	AbstractProcessor(final AppStatus appStatus, 
			final CompressionWorkerConfigurationProperties properties,
			final ObsClient obsClient) {
		this.appStatus = appStatus;
		this.properties = properties;
		this.obsClient = new UnrecoverableErrorAwareObsClient(obsClient, e -> appStatus.getStatus().setFatalError());
	}
	/**
	 * Check if thread interrupted
	 * 
	 * @throws InterruptedException
	 */
	protected final void checkThreadInterrupted() throws InterruptedException {
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
			final String errMess = String.format("%s: Timeout after %s seconds",  message, properties.getCompressionTimeout());
			
			LOGGER.debug(errMess);
			throw e;
		}
	}

	protected final void cleanCompressionProcessing(final AbstractMessage event, final ExecutorService procExecutorSrv) {
		procExecutorSrv.shutdownNow();
		try {
			procExecutorSrv.awaitTermination(properties.getRequestTimeout(), TimeUnit.SECONDS);
			// TODO send kill if fails
		} catch (final InterruptedException e) {
			// Conserves the interruption
			Thread.currentThread().interrupt();
		}
		try {
			LOGGER.info("Erasing local working directory for job {}", event);
			
			final Path p = Paths.get(properties.getWorkingDirectory());
			if (p.toFile().exists()) {
				try (Stream<Path> walk = Files.walk(p, FileVisitOption.FOLLOW_LINKS)) {
					walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
				}
			}
		} catch (final IOException e) {
			LOGGER.error("{} [code {}] Failed to erase local working directory", event,
					ErrorCode.INTERNAL_ERROR.getCode());
			this.appStatus.setError("PROCESSING");
		}
	}
	
	protected final ReportingMessage errorReportMessage(final Exception e) {
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
