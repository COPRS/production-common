package esa.s1pdgs.cpoc.compression.process;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.compression.config.ApplicationProperties;
import esa.s1pdgs.cpoc.compression.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.compression.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

@Service
public class CompressProcessor {
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
	private final ObsService obsService;

	/**
	 * MQI service for reading message
	 */
	private final GenericMqiService<CompressionJobDto> mqiService;

	/**
	 * MQI service for stopping the MQI
	 */
	private final StatusService mqiStatusService;

	@Autowired
	public CompressProcessor(final AppStatus appStatus, final ApplicationProperties properties,
			final ObsService obsService,
			@Qualifier("mqiServiceForLevelJobs") final GenericMqiService<CompressionJobDto> mqiService,
			@Qualifier("mqiServiceForStatus") final StatusService mqiStatusService) {
		this.appStatus = appStatus;
		this.properties = properties;
		this.obsService = obsService;

		this.mqiService = mqiService;
		this.mqiStatusService = mqiStatusService;
	}

	public void processTask() {
		LOGGER.trace("[MONITOR] [step 0] Waiting message");

		// ----------------------------------------------------------
		// Read Message
		// ----------------------------------------------------------
		LOGGER.trace("[MONITOR] [step 0] Waiting message");
		if (appStatus.isShallBeStopped()) {
			LOGGER.info("[MONITOR] [step 0] The wrapper shall be stopped");
			this.appStatus.forceStopping();
			return;
		}
		GenericMessageDto<CompressionJobDto> message = null;
		try {
			message = mqiService.next();
			this.appStatus.setWaiting();
		} catch (AbstractCodedException ace) {
			LOGGER.error("[MONITOR] [step 0] [code {}] {}", ace.getCode().getCode(), ace.getLogMessage());
			message = null;
			this.appStatus.setError("NEXT_MESSAGE");
		}
		if (message == null || message.getBody() == null) {
			LOGGER.trace("[MONITOR] [step 0] No message received: continue");
			return;
		}
		appStatus.setProcessing(message.getIdentifier());
		LOGGER.info("Initializing job processing {}", message);

		// ----------------------------------------------------------
		// Initialize processing
		// ------------------------------------------------------
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "CompressionProcessing");
        
        final Reporting report = reportingFactory.newReporting(0);
        report.reportStart("Start compression processing");
        
		CompressionJobDto job = message.getBody();

		// Initialize the pool processor executor
		PoolExecutorCallable procExecutor = new PoolExecutorCallable(properties, job, // getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS,
																						// job),
				"CompressionProcessor - process");
		ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		ExecutorCompletionService<Boolean> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);

		// Initialize the input downloader
//		FileDownloader inputDownloader = new FileDownloader(obsService, job.getWorkDirectory(), job.getInputs(),
//				this.properties.getSizeBatchDownload(),
//				// getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job),
//				"CompressionProcessor", procExecutor);
		
        // ----------------------------------------------------------
        // Process message
        // ----------------------------------------------------------
//        processJob(message, inputDownloader, outputProcessor, procExecutorSrv,
//                procCompletionSrv, procExecutor, report);

		report.reportStop("End compression processing");
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
	protected void waitForPoolProcessesEnding(final ExecutorCompletionService<Boolean> procCompletionSrv)
			throws InterruptedException, AbstractCodedException {
		checkThreadInterrupted();
		try {
			procCompletionSrv.take().get(properties.getTmProcAllTasksS(), TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof AbstractCodedException) {
				throw (AbstractCodedException) e.getCause();
			} else {
				throw new InternalErrorException(e.getMessage(), e);
			}
		} catch (TimeoutException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	/**
	 * @param job
	 * @param poolProcessing
	 * @param procExecutorSrv
	 */
	protected void cleanJobProcessing(final LevelJobDto job, final boolean poolProcessing,
			final ExecutorService procExecutorSrv) {
		if (poolProcessing) {
			procExecutorSrv.shutdownNow();
			try {
				procExecutorSrv.awaitTermination(properties.getTmProcStopS(), TimeUnit.SECONDS);
				// TODO send kill if fails
			} catch (InterruptedException e) {
				// Conserves the interruption
				Thread.currentThread().interrupt();
			}
		}
		this.eraseDirectory(job);
	}

	private void eraseDirectory(final LevelJobDto job) {
		try {
//                LOGGER.info("{} Erasing local working directory",
//                        getPrefixMonitorLog(MonitorLogUtils.LOG_ERASE, job));
			Path p = Paths.get(job.getWorkDirectory());
			Files.walk(p, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.peek(System.out::println).forEach(File::delete);
		} catch (IOException e) {
//                LOGGER.error(
//                        "{} [code {}] Failed to erase local working directory",
//                        getPrefixMonitorLog(MonitorLogUtils.LOG_ERASE, job),
//                        ErrorCode.INTERNAL_ERROR.getCode());
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
	protected void ackProcessing(final GenericMessageDto<LevelJobDto> dto, final boolean ackOk,
			final String errorMessage) {
		boolean stopping = appStatus.getStatus().isStopping();

		// Ack
		if (ackOk) {
			ackPositively(stopping, dto);
		} else {
			ackNegatively(stopping, dto, errorMessage);
		}

		// Check status
//        LOGGER.info("{} Checking status consumer",
//                getPrefixMonitorLog(MonitorLogUtils.LOG_STATUS, dto.getBody()));
		if (appStatus.getStatus().isStopping()) {
			// TODO send stop to the MQI
			try {
				mqiStatusService.stop();
			} catch (AbstractCodedException ace) {
//                LOGGER.error("{} {} Checking status consumer",
//                        getPrefixMonitorLog(MonitorLogUtils.LOG_STATUS,
//                                dto.getBody()),
//                        ace.getLogMessage());
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
	protected void ackNegatively(final boolean stop, final GenericMessageDto<LevelJobDto> dto,
			final String errorMessage) {
//        LOGGER.info("{} Acknowledging negatively",
//                getPrefixMonitorLog(MonitorLogUtils.LOG_ACK, dto.getBody()));
		try {
			mqiService.ack(new AckMessageDto(dto.getIdentifier(), Ack.ERROR, errorMessage, stop));
		} catch (AbstractCodedException ace) {
//            LOGGER.error("{} [step 5] {} [code {}] {}",
//                    getPrefixMonitorLog(MonitorLogUtils.LOG_DFT, dto.getBody()),
//                    getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR,
//                            dto.getBody()),
//                    ace.getCode().getCode(), ace.getLogMessage());
		}
		appStatus.setError("PROCESSING");
	}

	protected void ackPositively(final boolean stop, final GenericMessageDto<LevelJobDto> dto) {
//        LOGGER.info("{} Acknowledging positively",
//                getPrefixMonitorLog(MonitorLogUtils.LOG_ACK, dto.getBody()));
		try {
			mqiService.ack(new AckMessageDto(dto.getIdentifier(), Ack.OK, null, stop));
		} catch (AbstractCodedException ace) {
//            LOGGER.error("{} [step 5] {} [code {}] {}",
//                    getPrefixMonitorLog(MonitorLogUtils.LOG_DFT, dto.getBody()),
//                    getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR,
//                            dto.getBody()),
//                    ace.getCode().getCode(), ace.getLogMessage());
			appStatus.setError("PROCESSING");
		}
	}

}
