package esa.s1pdgs.cpoc.compression.process;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.config.ApplicationProperties;
import esa.s1pdgs.cpoc.compression.file.FileDownloader;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.compression.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

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
	private final GenericMqiService<LevelJobDto> mqiService;
	
    /**
     * MQI service for stopping the MQI
     */
    private final StatusService mqiStatusService;

	@Autowired
	public CompressProcessor(final AppStatus appStatus, final ApplicationProperties properties,
			final ObsService obsService,
			@Qualifier("mqiServiceForLevelJobs") final GenericMqiService<LevelJobDto> mqiService,
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
		GenericMessageDto<LevelJobDto> message = null;
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
		LevelJobDto job = message.getBody();

		// Initialize the pool processor executor
		PoolExecutorCallable procExecutor = new PoolExecutorCallable(properties, job, // getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS,
																						// job),
				"CompressionProcessor - process");
		ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		ExecutorCompletionService<Boolean> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);

		// Initialize the input downloader
		FileDownloader inputDownloader = new FileDownloader(obsService, job.getWorkDirectory(), job.getInputs(),
				this.properties.getSizeBatchDownload(),
				// getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job),
				"CompressionProcessor", procExecutor);

	}

}
