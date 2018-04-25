package fr.viveris.s1pdgs.level0.wrapper.controller;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.config.ApplicationProperties;
import fr.viveris.s1pdgs.level0.wrapper.config.DevProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException.ErrorCode;
import fr.viveris.s1pdgs.level0.wrapper.services.job.JobProcessor;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;

public class AbstractJobConsumer {

	/**
	 * Logger
	 */
	protected static final Logger LOGGER = LogManager.getLogger(L1JobConsumer.class);

	/**
	 * Amazon S3 servive for session and raw files
	 */
	protected final S3Factory s3Factory;

	protected final OutputProcuderFactory outputProcuderFactory;

	protected final DevProperties devProperties;
	
	protected final ApplicationProperties properties;

	// KAFKA listener endpoint registry
	protected final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
	protected final String listenerContainerId;

	protected final ExecutorService jobWorkerThreadPool;
	protected final CompletionService<Boolean> jobWorkerService;
	protected int nbCurrentTasks = 0;

	protected final AppStatus appStatus;

	/**
	 * @param s3Factory
	 * @param outputProcuderFactory
	 * @param sizeS3UploadBatch
	 * @param sizeS3DownloadBatch
	 */
	public AbstractJobConsumer(final S3Factory s3Factory, final OutputProcuderFactory outputProcuderFactory,
			final ApplicationProperties properties, final DevProperties devProperties,
			final AppStatus appStatus, final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
			final String listenerContainerId) {
		super();
		this.s3Factory = s3Factory;
		this.outputProcuderFactory = outputProcuderFactory;
		this.properties = properties;
		this.devProperties = devProperties;
		this.appStatus = appStatus;
		this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
		this.listenerContainerId = listenerContainerId;
		this.jobWorkerThreadPool = Executors.newSingleThreadExecutor();
		this.jobWorkerService = new ExecutorCompletionService<>(this.jobWorkerThreadPool);
	}

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	protected void internalReceive(JobDto job, Acknowledgment acknowledgment, String outputListFile) {

		// Initialize logs
		String prefixLog = "[MONITOR]";
		String prefixLogStart = String.format("%s [step 0] [productName %s] [workDir %s]", prefixLog,
				job.getProductIdentifier(), job.getWorkDirectory());

		LOGGER.info("{} Starting job generation", prefixLogStart);

		if (!this.appStatus.isShallBeStopped()) {

			this.appStatus.setProcessing();

			// Ack message
			this.ackMessage(acknowledgment, String.format("%s [step 1a] [productName %s] [workDir %s]", prefixLog,
					job.getProductIdentifier(), job.getWorkDirectory()));

			// Remove the last executed future if needed, wait for task ending if necessary
			if (this.nbCurrentTasks > 0) {
				this.cleanPreviousExecution(String.format("%s [step 1b] [productName %s] [workDir %s]", prefixLog,
						job.getProductIdentifier(), job.getWorkDirectory()));
			}

			// Launch job
			this.launchJob(job, outputListFile);

			// Set the consumer in pause
			this.pauseConsumer(String.format("%s [step 1c] [productName %s] [workDir %s]", prefixLog,
					job.getProductIdentifier(), job.getWorkDirectory()));

		} else {
			LOGGER.info("{} End job generation: nothing done because the wrapper shall be stopped", prefixLogStart);
			this.appStatus.forceStopping();
		}
	}

	private void ackMessage(Acknowledgment acknowledgment, String prefixLog) {
		LOGGER.info("{} Acknowledging message", prefixLog);
		try {
			acknowledgment.acknowledge();
		} catch (Exception e) {
			LOGGER.error("{} [code {}] Exception occurred during acknowledgment {}", prefixLog,
					ErrorCode.KAFKA_COMMIT_ERROR.getCode(), e.getMessage());
			this.appStatus.setError();
		}
	}

	private void cleanPreviousExecution(String prefixLog) {
		LOGGER.info("{} Resetting worker thread pool", prefixLog);
		try {
			Future<Boolean> future = this.jobWorkerService.poll(this.properties.getTimeoutProcessCheckStopS(), TimeUnit.SECONDS);
			if (future == null) {
				LOGGER.warn("{} Cannot retrieve last execution after {} seconds: force shutdown of previous job",
						prefixLog, this.properties.getTimeoutProcessCheckStopS());
				this.jobWorkerThreadPool.shutdownNow();
			}
		} catch (InterruptedException e) {
			LOGGER.error("{} [code {}] Exception occurred during resetting {}", prefixLog, e.getMessage());
			this.appStatus.setError();
		}
		this.nbCurrentTasks = 0;
	}

	private void launchJob(JobDto job, String outputListFile) {
		this.jobWorkerService.submit(new JobProcessor(job, this.appStatus, this.properties, this.devProperties, this.listenerContainerId,
				kafkaListenerEndpointRegistry, s3Factory, outputProcuderFactory, 
				outputListFile));
		nbCurrentTasks++;
	}

	private void pauseConsumer(String prefixLog) {
		LOGGER.info("{} Setting the consumer in pause", prefixLog);
		MessageListenerContainer listenerContainer = this.kafkaListenerEndpointRegistry
				.getListenerContainer(this.listenerContainerId);
		if (listenerContainer != null) {
			listenerContainer.pause();
		} else {
			LOGGER.warn("{} Cannot retrieve listenerContainer", prefixLog);
		}
	}

}
