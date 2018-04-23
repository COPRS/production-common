package fr.viveris.s1pdgs.level0.wrapper.controller;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.config.DevProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.services.job.JobProcessingResult;
import fr.viveris.s1pdgs.level0.wrapper.services.job.JobProcessor;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;

public class AbstractJobConsumer {

	/**
	 * Logger
	 */
	protected static final Logger LOGGER = LoggerFactory.getLogger(L1JobConsumer.class);

	/**
	 * Amazon S3 servive for session and raw files
	 */
	protected final S3Factory s3Factory;

	protected final OutputProcuderFactory outputProcuderFactory;

	protected final int sizeS3UploadBatch;

	protected final int sizeS3DownloadBatch;

	protected final DevProperties devProperties;

	// KAFKA listener endpoint registry
	protected final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
	protected final String listenerContainerId;

	protected final ExecutorService jobWorkerThreadPool;
	protected final CompletionService<JobProcessingResult> jobWorkerService;
	protected int nbCurrentTasks = 0;

	protected final AppStatus appStatus;

	/**
	 * @param s3Factory
	 * @param outputProcuderFactory
	 * @param sizeS3UploadBatch
	 * @param sizeS3DownloadBatch
	 */
	public AbstractJobConsumer(final S3Factory s3Factory, final OutputProcuderFactory outputProcuderFactory,
			final int sizeS3UploadBatch, final int sizeS3DownloadBatch, final DevProperties devProperties,
			final AppStatus appStatus, final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
			final String listenerContainerId) {
		super();
		this.s3Factory = s3Factory;
		this.outputProcuderFactory = outputProcuderFactory;
		this.sizeS3UploadBatch = sizeS3UploadBatch;
		this.sizeS3DownloadBatch = sizeS3DownloadBatch;
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
		LOGGER.info("[MONITOR] [Step 0] [productName {}] [workDir {}] Starting job generation",
				job.getProductIdentifier(), job.getWorkDirectory());
		if (!this.appStatus.isShallBeStopped()) {

			this.appStatus.setProcessing();

			try {

				// Ack message
				LOGGER.info("[MONITOR] [Step 1] [productName {}] [workDir {}] Acknowledging message",
						job.getProductIdentifier(), job.getWorkDirectory());
				try {
					acknowledgment.acknowledge();
				} catch (Exception e) {
					LOGGER.error(
							"[MONITOR] [Step 1] [productName {}] [workDir {}] Exception occurred during acknowledgment {}",
							job.getProductIdentifier(), job.getWorkDirectory(), e.getMessage());
					this.appStatus.setError();
				}

				// Remove the last executed future if needed, wait for task ending if necessary
				if (this.nbCurrentTasks > 0) {
					LOGGER.info("[MONITOR] [Step 1-b] [productName {}] [workDir {}] Resetting worker thread pool",
							job.getProductIdentifier(), job.getWorkDirectory());
					try {
						Future<JobProcessingResult> future = this.jobWorkerService.poll(10, TimeUnit.SECONDS);
						if (future == null) {
							LOGGER.warn(
									"[MONITOR] [Step 1-b] [productName {}] [workDir {}] Cannot retrieve last execution after 10 seconds: force shutdown of previous job",
									job.getProductIdentifier(), job.getWorkDirectory());
							this.jobWorkerThreadPool.shutdownNow();
							this.jobWorkerService.poll(500, TimeUnit.MILLISECONDS);
						}
					} catch (InterruptedException e) {
						LOGGER.error(
								"[MONITOR] [Step 1-b] [productName {}] [workDir {}] Exception occurred during resetting {}",
								job.getProductIdentifier(), job.getWorkDirectory(), e.getMessage());
						this.appStatus.setError();
					}
					this.nbCurrentTasks = 0;
				}

				// Launch job
				this.jobWorkerService.submit(new JobProcessor(s3Factory, outputProcuderFactory,
						kafkaListenerEndpointRegistry, sizeS3UploadBatch, sizeS3DownloadBatch, this.devProperties,
						this.listenerContainerId, job, this.appStatus, outputListFile));
				nbCurrentTasks++;

				// Set the consumer in pause
				LOGGER.info("[MONITOR] [Step 1-b] [productName {}] [workDir {}] Setting the consumer in pause",
						job.getProductIdentifier(), job.getWorkDirectory());
				MessageListenerContainer listenerContainer = this.kafkaListenerEndpointRegistry
						.getListenerContainer(this.listenerContainerId);
				if (listenerContainer != null) {
					listenerContainer.pause();
				} else {
					LOGGER.warn("[MONITOR] [Step 1-b] [productName {}] [workDir {}] Cannot retrieve listenerContainer",
							job.getProductIdentifier(), job.getWorkDirectory());
				}

			} catch (Exception e) {
				LOGGER.error("[MONITOR] [productName {}] [workDir {}] Exception occurred : {}",
						job.getProductIdentifier(), job.getWorkDirectory(), e.getMessage());
				this.appStatus.setError();
			}
			
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException ie) {

			}
			LOGGER.info(
					"[MONITOR] [Step 0] [productName {}] [workDir {}] End job generation: nothing done because the wrapper shall be stopped",
					job.getProductIdentifier(), job.getWorkDirectory());
		}
	}

}
