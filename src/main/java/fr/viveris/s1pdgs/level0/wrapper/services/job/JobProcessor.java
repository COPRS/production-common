package fr.viveris.s1pdgs.level0.wrapper.services.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.config.ApplicationProperties;
import fr.viveris.s1pdgs.level0.wrapper.config.DevProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException.ErrorCode;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.services.file.InputDownloader;
import fr.viveris.s1pdgs.level0.wrapper.services.file.OutputProcessor;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;
import fr.viveris.s1pdgs.level0.wrapper.services.task.PoolExecutorCallable;

public class JobProcessor implements Callable<Boolean> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(JobProcessor.class);

	private final JobDto job;

	private final AppStatus appStatus;

	private final InputDownloader inputDownloader;

	private final OutputProcessor outputProcessor;

	private final DevProperties devProperties;

	private final ApplicationProperties properties;

	private final ExecutorService poolProcessExecutorService;
	private final CompletionService<Boolean> poolProcessCompletionService;
	private final PoolExecutorCallable poolProcessorExecutor;

	// KAFKA listener endpoint registry
	private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
	private final String kafkaListenerContainerId;

	private final String prefixLog;
	private final String prefixLogPoolProcessor;
	private final String prefixLogInputDownloader;
	private final String prefixLogPoolOutputProcessor;
	private final String prefixLogErasing;
	private final String prefixLogResuming;
	private final String prefixLogStatus;
	private final String prefixLogEnd;
	private final String prefixLogError;

	/**
	 * Constructor
	 * 
	 * @param job
	 * @param appStatus
	 * @param devProperties
	 * @param kafkaListenerContainerId
	 * @param kafkaListenerEndpointRegistry
	 * @param s3Factory
	 * @param outputProcuderFactory
	 * @param sizeS3UploadBatch
	 * @param sizeS3DownloadBatch
	 * @param outputListFile
	 */
	public JobProcessor(final JobDto job, final AppStatus appStatus, final ApplicationProperties properties,
			final DevProperties devProperties, final String kafkaListenerContainerId,
			final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, final S3Factory s3Factory,
			final OutputProcuderFactory outputProcuderFactory, final String outputListFile) {
		super();

		this.job = job;
		this.appStatus = appStatus;
		this.devProperties = devProperties;
		this.properties = properties;
		this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
		this.kafkaListenerContainerId = kafkaListenerContainerId;

		// initalize logs
		this.prefixLog = "[MONITOR]";
		this.prefixLogPoolProcessor = String.format("%s [step 3] [productName %s] [workDir %s]", this.prefixLog,
				job.getProductIdentifier(), job.getWorkDirectory());
		this.prefixLogInputDownloader = String.format("%s [step 2] [productName %s] [workDir %s]", this.prefixLog,
				job.getProductIdentifier(), job.getWorkDirectory());
		this.prefixLogPoolOutputProcessor = String.format("%s [step 4] [productName %s] [workDir %s]", this.prefixLog,
				job.getProductIdentifier(), job.getWorkDirectory());
		this.prefixLogErasing = String.format("%s [step 5] [productName %s] [workDir %s]", this.prefixLog,
				job.getProductIdentifier(), job.getWorkDirectory());
		this.prefixLogResuming = String.format("%s [step 6] [productName %s] [workDir %s]", this.prefixLog,
				job.getProductIdentifier(), job.getWorkDirectory());
		this.prefixLogStatus = String.format("%s [step 7] [productName %s] [workDir %s]", this.prefixLog,
				job.getProductIdentifier(), job.getWorkDirectory());
		this.prefixLogEnd = String.format("%s [step 0] [productName %s] [workDir %s]", this.prefixLog,
				job.getProductIdentifier(), job.getWorkDirectory());
		this.prefixLogError = String.format("[productName %s] [workDir %s]", job.getProductIdentifier(),
				job.getWorkDirectory());

		// Initialize the pool processor executor
		this.poolProcessorExecutor = new PoolExecutorCallable(this.properties, this.job, this.prefixLogPoolProcessor);
		this.poolProcessExecutorService = Executors.newSingleThreadExecutor();
		this.poolProcessCompletionService = new ExecutorCompletionService<>(poolProcessExecutorService);

		// Initialize the input downloader
		inputDownloader = new InputDownloader(s3Factory, this.job.getWorkDirectory(), this.job.getInputs(),
				this.properties.getSizeBatchS3Download(), this.prefixLogInputDownloader, poolProcessorExecutor,
				this.properties.getLevel());

		// Initiliaze the outpt processor
		outputProcessor = new OutputProcessor(s3Factory, outputProcuderFactory, this.job.getWorkDirectory(),
				this.job.getOutputs(), outputListFile, this.properties.getSizeBatchS3Upload(),
				this.prefixLogPoolOutputProcessor);

	}

	/**
	 * Execute a job
	 */
	@Override
	public Boolean call() throws Exception {
		int step = 2;
		boolean poolProcessorInProgress = false;

		try {
			step = 3;
			if (devProperties.getStepsActivation().get("execution")) {
				this.processPoolProcesses();
				poolProcessorInProgress = true;
			} else {
				LOGGER.info("{} Executing processes bypassed", this.prefixLogPoolProcessor);
			}

			step = 2;
			if (devProperties.getStepsActivation().get("download")) {
				this.processInputs();
			} else {
				LOGGER.info("{} Preparing local working directory bypassed", this.prefixLogInputDownloader);
			}

			step++;
			if (devProperties.getStepsActivation().get("execution")) {
				this.waitForPoolProcessesEnding();
				poolProcessorInProgress = false;
			} else {
				LOGGER.info("{} Executing processes bypassed", this.prefixLogPoolProcessor);
			}

			step++;
			if (devProperties.getStepsActivation().get("upload")) {
				this.processOutputs();
			} else {
				LOGGER.info("{} Processing l0 outputs bypasssed", prefixLogPoolOutputProcessor);
			}

		} catch (CodedException e) {
			// Log occurred error
			LOGGER.error("{} [step {}] {} [code {}] {}", this.prefixLog, step, this.prefixLogError,
					e.getCode().getCode(), e.getMessage());
			this.appStatus.setError();

		} finally {

			if (poolProcessorInProgress) {
				this.terminateProcessesExecution();
			}

			if (devProperties.getStepsActivation().get("erasing")) {
				this.eraseLocalDirectory();
			} else {
				LOGGER.info("{} Erasing local working directory bypassed", this.prefixLogErasing);
			}

			this.resumeConsumer();

			this.checkingStatus();
		}

		LOGGER.info("{} End L0 job generation", this.prefixLogEnd);

		return true;
	}

	private void processPoolProcesses() {
		LOGGER.info("{} Starting process executor", this.prefixLogPoolProcessor);
		this.poolProcessCompletionService.submit(this.poolProcessorExecutor);
	}

	private void waitForPoolProcessesEnding() throws InterruptedException, CodedException {
		if (!Thread.currentThread().isInterrupted()) {
			try {
				this.poolProcessCompletionService.take().get(this.properties.getTimeoutProcessAllTasksS(),
						TimeUnit.SECONDS);
			} catch (ExecutionException e) {
				if (e.getCause().getClass().isAssignableFrom(CodedException.class)) {
					throw (CodedException) e.getCause();
				} else {
					throw new InternalErrorException(e.getMessage(), e);
				}
			} catch (TimeoutException e) {
				throw new InternalErrorException(e.getMessage(), e);
			}
		} else {
			throw new InterruptedException("Current thread is interrupted");
		}
	}

	private void terminateProcessesExecution() {
		this.poolProcessExecutorService.shutdownNow();
		try {
			if (!this.poolProcessExecutorService.awaitTermination(this.properties.getTimeoutProcessStopS(),
					TimeUnit.SECONDS)) {
				// TODO send kill
			}
		} catch (InterruptedException e) {
			// Conserves the interruption
			Thread.currentThread().interrupt();
		}
	}

	private void processInputs() throws CodedException, InterruptedException {
		if (!Thread.currentThread().isInterrupted()) {
			LOGGER.info("{} Preparing local working directory", this.prefixLogInputDownloader);
			inputDownloader.processInputs();
		} else {
			throw new InterruptedException("Current thread is interrupted");
		}
	}

	private void processOutputs() throws InterruptedException, CodedException {
		if (!Thread.currentThread().isInterrupted()) {
			LOGGER.info("{} Processing l0 outputs", prefixLogPoolOutputProcessor);
			outputProcessor.processOutput();
		} else {
			throw new InterruptedException("Current thread is interrupted");
		}
	}

	private void eraseLocalDirectory() {
		try {
			LOGGER.info("{} Erasing local working directory", this.prefixLogErasing);
			Path p = Paths.get(job.getWorkDirectory());
			Files.walk(p, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.peek(System.out::println).forEach(File::delete);
		} catch (IOException e) {
			LOGGER.error("{} [code {}] Failed to erase local working directory", this.prefixLogErasing,
					ErrorCode.INTERNAL_ERROR.getCode());
			this.appStatus.setError();
		}
	}

	private void resumeConsumer() {
		LOGGER.info("{} Resuming consumer", this.prefixLogResuming);
		if (this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaListenerContainerId) != null) {
			this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaListenerContainerId).resume();
		} else {
			LOGGER.info("{} [code {}] Cannot resume consumer because no listener {}", this.prefixLogResuming,
					ErrorCode.KAFKA_RESUMING_ERROR.getCode(), this.kafkaListenerContainerId);
		}
	}

	private void checkingStatus() {
		LOGGER.info("{} Checking status consumer", this.prefixLogStatus);
		if (this.appStatus.getStatus().isStopping()) {
			System.exit(0);
		} else if (this.appStatus.getStatus().isFatalError()) {
			System.exit(-1);
		} else {
			this.appStatus.setWaiting();
		}
	}
}
