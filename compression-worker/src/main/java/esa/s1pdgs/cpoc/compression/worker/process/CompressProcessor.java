package esa.s1pdgs.cpoc.compression.worker.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.compression.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.compression.worker.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.worker.file.FileUploader;
import esa.s1pdgs.cpoc.compression.worker.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

@Service
public class CompressProcessor implements MqiListener<CompressionJob> {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(CompressProcessor.class);

	/**
	 * Application properties
	 */
	private final ApplicationProperties properties;

	/**
	 * Application status
	 */
	private final AppStatus appStatus;

	/**
	 * Output processsor
	 */
	private final ObsClient obsClient;

	/**
	 * Output processsor
	 */
	private final OutputProducerFactory producerFactory;

	/**
	 * MQI service for reading message
	 */
	private final GenericMqiClient mqiClient;

	/**
	 * MQI service for stopping the MQI
	 */
	private final StatusService mqiStatusService;

	private final ErrorRepoAppender errorAppender;
	
	private final long pollingIntervalMs;
	
	private final long pollingInitialDelayMs;

	@Autowired
	public CompressProcessor(final AppStatus appStatus, final ApplicationProperties properties,
			final ObsClient obsClient, final OutputProducerFactory producerFactory,
			final GenericMqiClient mqiClient,
			final ErrorRepoAppender errorAppender,
			final StatusService mqiStatusService,
			@Value("${compression-worker.fixed-delay-ms}") final long pollingIntervalMs,
			@Value("${compression-worker.init-delay-poll-ms}") final long pollingInitialDelayMs) {
		this.appStatus = appStatus;
		this.properties = properties;
		this.obsClient = obsClient;
		this.producerFactory = producerFactory;
		this.mqiClient = mqiClient;
		this.mqiStatusService = mqiStatusService;
		this.errorAppender = errorAppender;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}
	
	@PostConstruct
	public void initService() {
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<CompressionJob>(mqiClient, ProductCategory.COMPRESSION_JOBS, this,
					pollingIntervalMs, pollingInitialDelayMs, esa.s1pdgs.cpoc.appstatus.AppStatus.NULL));
		}
	}

	/**
	 * Consume and execute jobs
	 */
	@Override
	public void onMessage(final GenericMessageDto<CompressionJob> message) {

		appStatus.setProcessing(message.getId());
		LOGGER.info("Initializing job processing {}", message);

		// ----------------------------------------------------------
		// Initialize processing
		// ------------------------------------------------------
		final Reporting report = ReportingUtils.newReportingBuilderFor("CompressionProcessing")
				.newWorkerComponentReporting();

		final String workDir = properties.getWorkingDirectory();
		report.begin(
				new FilenameReportingInput(message.getBody().getKeyObjectStorage()),
				new ReportingMessage("Start compression processing")
		);

		final CompressionJob job = message.getBody();

		// Initialize the pool processor executor
		final CompressExecutorCallable procExecutor = new CompressExecutorCallable(job, // getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS,
																					// job),
				"CompressionProcessor - process", properties);
		final ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);

		// Initialize the input downloader
		final FileDownloader fileDownloader = new FileDownloader(obsClient, workDir, job,
				this.properties.getSizeBatchDownload(),
				// getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job),
				"CompressionProcessor");

		final FileUploader fileUploader = new FileUploader(obsClient, producerFactory, workDir, message, job);

		// ----------------------------------------------------------
		// Process message
		// ----------------------------------------------------------
		final String outputName = processTask(message, fileDownloader, fileUploader, procExecutorSrv, procCompletionSrv, procExecutor, report);

		report.end(
				new FilenameReportingOutput(outputName), 
				new ReportingMessage("End compression processing")
		);
	}

	protected String processTask(final GenericMessageDto<CompressionJob> message, final FileDownloader fileDownloader,
			final FileUploader fileUploader, final ExecutorService procExecutorSrv,
			final ExecutorCompletionService<Void> procCompletionSrv, final CompressExecutorCallable procExecutor,
			final Reporting report) {
		final CompressionJob job = message.getBody();
		int step = 0;
		boolean ackOk = false;
		String errorMessage = "";
		String filename = "NOT_DEFINED";
		
		FailedProcessingDto failedProc = null;

		try {
			step = 2;

			checkThreadInterrupted();
			LOGGER.info("{} Preparing local working directory", "LOG_INPUT", // getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT
					job);
			fileDownloader.processInputs();

			step = 3;
			LOGGER.info("{} Starting process executor", "LOG PROCESS"// getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS
					, job);
			procCompletionSrv.submit(procExecutor);

			step = 3;
			this.waitForPoolProcessesEnding(procCompletionSrv);
			step = 4;
			checkThreadInterrupted();
			LOGGER.info("{} Processing l0 outputs", "LOG_OUTPUT", // getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT
					job);

			filename = fileUploader.processOutput();

			ackOk = true;
		} catch (final AbstractCodedException ace) {
			ackOk = false;

			errorMessage = String.format(
					"[s1pdgsCompressionTask] [subTask processing] [STOP KO] %s [step %d] %s [code %d] %s", "LOG_DFT", // getPrefixMonitorLog(MonitorLogUtils.LOG_DFT,
																														// job),
					step, "LOG_ERROR", // getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR, job),
					ace.getCode().getCode(), ace.getLogMessage());
			report.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));

			failedProc = new FailedProcessingDto(properties.getHostname(), new Date(), errorMessage, message);

		} catch (final InterruptedException e) {
			ackOk = false;
			errorMessage = String.format(
					"%s [step %d] %s [code %d] [s1pdgsCompressionTask] [STOP KO] [subTask processing] [msg interrupted exception]",
					"LOG_DFT", // getPrefixMonitorLog(MonitorLogUtils.LOG_DFT, job),
					step, "LOG_ERROR", // getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR, job),
					ErrorCode.INTERNAL_ERROR.getCode());
			report.error(new ReportingMessage("Interrupted job processing"));
			failedProc = new FailedProcessingDto(properties.getHostname(), new Date(), errorMessage, message);
			cleanCompressionProcessing(job, procExecutorSrv);
		} catch (ObsEmptyFileException e) {
			ackOk = false;
			report.error(new ReportingMessage(LogUtils.toString(e)));
			failedProc = new FailedProcessingDto(properties.getHostname(), new Date(), errorMessage, message);
		}

		// Ack and check if application shall stopped
		ackProcessing(message, failedProc, ackOk, errorMessage);
		return filename;
	}

	/**
	 * Check if thread interrupted
	 * 
	 * @throws InterruptedException
	 */
	protected void checkThreadInterrupted() throws InterruptedException {
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
	protected void waitForPoolProcessesEnding(final ExecutorCompletionService<?> procCompletionSrv)
			throws InterruptedException, AbstractCodedException {
		checkThreadInterrupted();
		try {
			procCompletionSrv.take().get(properties.getTmProcAllTasksS(), TimeUnit.SECONDS);
		} catch (final ExecutionException e) {
			if (e.getCause() instanceof AbstractCodedException) {
				throw (AbstractCodedException) e.getCause();
			} else {
				throw new InternalErrorException(e.getMessage(), e);
			}
		} catch (final TimeoutException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	/**
	 * @param job
	 * @param poolProcessing
	 * @param procExecutorSrv
	 */
	protected void cleanCompressionProcessing(final CompressionJob job, final ExecutorService procExecutorSrv) {
		procExecutorSrv.shutdownNow();
		try {
			procExecutorSrv.awaitTermination(properties.getTmProcStopS(), TimeUnit.SECONDS);
			// TODO send kill if fails
		} catch (final InterruptedException e) {
			// Conserves the interruption
			Thread.currentThread().interrupt();
		}

		this.eraseDirectory(job);
	}

	private void eraseDirectory(final CompressionJob job) {
		try {
			LOGGER.info("Erasing local working directory for job {}", job);
			final Path p = Paths.get(properties.getWorkingDirectory());
			Files.walk(p, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.peek(System.out::println).forEach(File::delete);
		} catch (final IOException e) {
			LOGGER.error("{} [code {}] Failed to erase local working directory", job,
					ErrorCode.INTERNAL_ERROR.getCode());
			this.appStatus.setError("PROCESSING");
		}
	}

	/**
	 * Ack job processing and stop app if needed
	 * 
	 * @param dto
	 * @param ackOk
	 * @param errorMessage
	 */
	protected void ackProcessing(final GenericMessageDto<CompressionJob> dto,
			final FailedProcessingDto failed, final boolean ackOk,
			final String errorMessage) {
		final boolean stopping = appStatus.getStatus().isStopping();

		// Ack
		if (ackOk) {
			ackPositively(stopping, dto);
		} else {
			ackNegatively(stopping, dto, errorMessage);
			errorAppender.send(failed);
		}

		// Check status
        LOGGER.info("Checking status consumer {}", dto.getBody());
		if (appStatus.getStatus().isStopping()) {
			// TODO send stop to the MQI
			try {
				mqiStatusService.stop();
			} catch (final AbstractCodedException ace) {
				LOGGER.error("MQI service couldn't be stopped {}",ace);
			}
			System.exit(0);
		} else if (appStatus.getStatus().isFatalError()) {
			System.exit(-1);
		} else {
			appStatus.setWaiting();
		}
	}

	/**
	 * @param dto
	 * @param errorMessage
	 */
	protected void ackNegatively(final boolean stop, final GenericMessageDto<CompressionJob> dto,
			final String errorMessage) {
        LOGGER.info("Acknowledging negatively {} ",dto.getBody());
		try {
			mqiClient.ack(new AckMessageDto(dto.getId(), Ack.ERROR, errorMessage, stop), 
					ProductCategory.COMPRESSION_JOBS);
		} catch (final AbstractCodedException ace) {
			LOGGER.error("Unable to confirm negatively request:{}",ace);
		}
		appStatus.setError("PROCESSING");
	}

	protected void ackPositively(final boolean stop, final GenericMessageDto<CompressionJob> dto) {
		LOGGER.info("Acknowledging positively {}", dto.getBody());
		try {
			mqiClient.ack(new AckMessageDto(dto.getId(), Ack.OK, null, stop), 
					ProductCategory.COMPRESSION_JOBS);
		} catch (final AbstractCodedException ace) {
			LOGGER.error("Unable to confirm positively request:{}",ace);
			appStatus.setError("PROCESSING");
		}
	}

}
