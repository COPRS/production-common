package esa.s1pdgs.cpoc.compression.worker.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.compression.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.compression.worker.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.worker.file.FileUploader;
import esa.s1pdgs.cpoc.compression.worker.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.compression.worker.process.CompressExecutorCallable;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class CompressProcessor implements MqiListener<CompressionJob> {
	private static final Logger LOGGER = LogManager.getLogger(CompressProcessor.class);

	private final ApplicationProperties properties;
	private final AppStatus appStatus;
	private final ObsClient obsClient;
	private final OutputProducerFactory producerFactory;
	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final StatusService mqiStatusService;
	private final ErrorRepoAppender errorAppender;
	private final long pollingIntervalMs;
	private final long pollingInitialDelayMs;

	@Autowired
	public CompressProcessor(
			final AppStatus appStatus, 
			final ApplicationProperties properties,
			final ObsClient obsClient, 
			final OutputProducerFactory producerFactory,
			final GenericMqiClient mqiClient,
			final List<MessageFilter> messageFilter,
			final ErrorRepoAppender errorAppender,
			final StatusService mqiStatusService,
			@Value("${compression-worker.fixed-delay-ms}") final long pollingIntervalMs,
			@Value("${compression-worker.init-delay-poll-ms}") final long pollingInitialDelayMs
	) {
		this.appStatus = appStatus;
		this.properties = properties;
		this.obsClient = obsClient;
		this.producerFactory = producerFactory;
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.mqiStatusService = mqiStatusService;
		this.errorAppender = errorAppender;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}
	
	@PostConstruct
	public void initService() {
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(newMqiConsumer());
		}
	}

	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<CompressionJob> message) throws Exception {
		final String workDir = properties.getWorkingDirectory();
		final CompressionJob job = message.getBody();
		
		final Reporting report = ReportingUtils.newReportingBuilder()
				.predecessor(job.getUid())				
				.newReporting("CompressionProcessing");
		
		// Initialize the pool processor executor
		final CompressExecutorCallable procExecutor = new CompressExecutorCallable(job, properties);
		final ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);

		// Initialize the input downloader
		final FileDownloader fileDownloader = new FileDownloader(
				obsClient, 
				workDir, 
				job,
				properties.getSizeBatchDownload()
		);
		final FileUploader fileUploader = new FileUploader(obsClient, producerFactory, workDir, message, job, report.getUid());	
		report.begin(
				ReportingUtils.newFilenameReportingInputFor(job.getProductFamily(), message.getBody().getKeyObjectStorage()),
				new ReportingMessage("Start compression processing")
		);		
		return new MqiMessageEventHandler.Builder<CompressionEvent>(ProductCategory.COMPRESSED_PRODUCTS)
				.onSuccess(res -> report.end(
						ReportingUtils.newFilenameReportingOutputFor(job.getProductFamily(),job.getOutputKeyObjectStorage()), 
						new ReportingMessage("End compression processing")
				))
				.onError(e -> report.error(errorReportMessage(e)))
				.publishMessageProducer(() -> {
					try {
						if(skip(job)) {							
							LOGGER.warn("Skipping uncompression for {}", job);
							return new MqiPublishingJob<CompressionEvent>(Collections.emptyList());							
						} 
						checkThreadInterrupted();
						LOGGER.info("Downloading inputs for {}", job);
						fileDownloader.processInputs(report);
			
						checkThreadInterrupted();
						LOGGER.info("Compressing/Uncompressing inputs for {}", job);
						final Future<?> fut = procCompletionSrv.submit(procExecutor);
						waitForPoolProcessesEnding(
								"Compressing/Uncompressing inputs for " + job,
								fut,
								procCompletionSrv, 
								properties.getTmProcAllTasksS() * 1000L);
			
						checkThreadInterrupted();
						LOGGER.info("Uploading compressed/uncompressed outputs for {}", job);
						final List<GenericPublicationMessageDto<CompressionEvent>> compressionEventDtos = fileUploader.processOutput(report);
						
						// ugly workaround for impossible casting issue:
						final List<GenericPublicationMessageDto<? extends AbstractMessage>> result = new ArrayList<>();
						for (final GenericPublicationMessageDto<CompressionEvent> compressionEventDto : compressionEventDtos) {
							result.add(compressionEventDto);
						}
						return new MqiPublishingJob<CompressionEvent>(result);
					}
					finally {
						// initially, this has only been performed on InterruptedException but we discussed that it makes sense to
						// always perform the cleanup, also see S1PRO-988 
						cleanCompressionProcessing(job, procExecutorSrv);
					}
				})
				.newResult();
	}
		
	private boolean skip(final CompressionJob job) throws ObsServiceException, SdkClientException {
		
		if (job.getCompressionDirection() == CompressionDirection.UNCOMPRESS) {
			LOGGER.debug("compression direction is: uncompress");
			
			final ObsObject obsObject = new ObsObject(job.getOutputProductFamily(), job.getOutputKeyObjectStorage());
			if(obsClient.prefixExists(obsObject)) {
				LOGGER.info(
				String.format(
						"OBS file '%s' (%s) already exist", 
						job.getOutputKeyObjectStorage(), 
						job.getOutputProductFamily()
				));
				return true;
			}
		}
		return false;
	}

	@Override
	public final void onTerminalError(final GenericMessageDto<CompressionJob> message, final Exception error) {		
		LOGGER.error(error);
		errorAppender.send(new FailedProcessingDto(
				properties.getHostname(), 
				new Date(), 
				String.format("Error on handling compression/uncompression for message %s: %s", message.getId(), LogUtils.toString(error)), 
				message
		));
		exitOnAppStatusStopOrWait();
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
			LOGGER.debug("{}: Timeout after {} seconds", message, properties.getTmProcAllTasksS());
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	private final void cleanCompressionProcessing(final CompressionJob job, final ExecutorService procExecutorSrv) {
		procExecutorSrv.shutdownNow();
		try {
			procExecutorSrv.awaitTermination(properties.getTmProcStopS(), TimeUnit.SECONDS);
			// TODO send kill if fails
		} catch (final InterruptedException e) {
			// Conserves the interruption
			Thread.currentThread().interrupt();
		}
		try {
			LOGGER.info("Erasing local working directory for job {}", job);
			
			final Path p = Paths.get(properties.getWorkingDirectory());
			if (p.toFile().exists()) {
				Files.walk(p, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.forEach(File::delete);
			}
		} catch (final IOException e) {
			LOGGER.error("{} [code {}] Failed to erase local working directory", job,
					ErrorCode.INTERNAL_ERROR.getCode());
			this.appStatus.setError("PROCESSING");
		}
	}
	
	private final MqiConsumer<CompressionJob> newMqiConsumer() {
		return new MqiConsumer<CompressionJob>(
				mqiClient, 
				ProductCategory.COMPRESSION_JOBS, 
				this,
				messageFilter,
				pollingIntervalMs, 
				pollingInitialDelayMs, 
				appStatus
		);
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
	
	// checks AppStatus, whether app shall be stopped and in that case, shut down this service as well
	private final void exitOnAppStatusStopOrWait() {
		if (appStatus.getStatus().isStopping()) {
			// TODO send stop to the MQI
			try {
				mqiStatusService.stop();
			} catch (final AbstractCodedException ace) {
				LOGGER.error("MQI service couldn't be stopped {}", ace);
			}
			appStatus.setShallBeStopped(true);
			appStatus.forceStopping(); // only stops when isShallBeStopped() == true
		} else if (appStatus.getStatus().isFatalError()) {
			appStatus.setShallBeStopped(true);
			appStatus.forceStopping(); // only stops when isShallBeStopped() == true
		} else {
			appStatus.setWaiting();
		}
	}
}
