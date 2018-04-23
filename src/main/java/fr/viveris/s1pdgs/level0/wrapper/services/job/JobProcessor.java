package fr.viveris.s1pdgs.level0.wrapper.services.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.config.DevProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InputDownloaderException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ProcessExecutionException;
import fr.viveris.s1pdgs.level0.wrapper.services.file.InputDownloader;
import fr.viveris.s1pdgs.level0.wrapper.services.file.OutputProcessor;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;
import fr.viveris.s1pdgs.level0.wrapper.services.task.PoolProcessor;

public class JobProcessor implements Callable<JobProcessingResult> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JobProcessor.class);

	/**
	 * Amazon S3 servive for session and raw files
	 */
	private final S3Factory s3Factory;

	private final OutputProcuderFactory outputProcuderFactory;

	private final int sizeS3UploadBatch;

	private final int sizeS3DownloadBatch;

	private final DevProperties devProperties;

	// KAFKA listener endpoint registry
	private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
	private final String kafkaListenerContainerId;

	private final JobDto job;

	private final AppStatus appStatus;
	
	private final String outputListFile;

	public JobProcessor(final S3Factory s3Factory, final OutputProcuderFactory outputProcuderFactory,
			final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, final int sizeS3UploadBatch,
			final int sizeS3DownloadBatch, final DevProperties devProperties, final String kafkaListenerContainerId,
			final JobDto job, final AppStatus appStatus, final String outputListFile) {
		super();
		this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
		this.s3Factory = s3Factory;
		this.outputProcuderFactory = outputProcuderFactory;
		this.sizeS3UploadBatch = sizeS3UploadBatch;
		this.sizeS3DownloadBatch = sizeS3DownloadBatch;
		this.devProperties = devProperties;
		this.kafkaListenerContainerId = kafkaListenerContainerId;
		this.job = job;
		this.appStatus = appStatus;
		this.outputListFile = outputListFile;
	}

	@Override
	public JobProcessingResult call() throws Exception {
		Boolean activation = null;

		try {

			activation = devProperties.getStepsActivation().get("download");
			if (activation == null || activation) {
				LOGGER.info("[MONITOR] [Step 2] [productName {}] [workDir {}] Preparing local working directory",
						job.getProductIdentifier(), job.getWorkDirectory());
				InputDownloader inputDownloader = new InputDownloader(this.s3Factory, job.getWorkDirectory(),
						job.getInputs(), this.sizeS3DownloadBatch,
						String.format("[MONITOR] [Step 2] [productName %s] [workDir %s]", job.getProductIdentifier(),
								job.getWorkDirectory()));
				inputDownloader.processInputs();
			} else {
				LOGGER.info(
						"[MONITOR] [Step 2] [productName {}] [workDir {}] Preparing local working directory bypassed",
						job.getProductIdentifier(), job.getWorkDirectory());
			}

			activation = devProperties.getStepsActivation().get("execution");
			if (activation == null || activation) {
				int counter = 0;
				for (JobPoolDto pool : job.getPools()) {
					counter++;
					LOGGER.info("[MONITOR] [Step 3] [productName {}] [workDir {}] [pool {}] Executing processes",
							job.getProductIdentifier(), job.getWorkDirectory(), counter);
					PoolProcessor taskProcessor = new PoolProcessor(pool, job.getJobOrder(), job.getWorkDirectory(),
							String.format("[MONITOR] [Step 3] [productName %s] [workDir %s] [pool %d]",
									job.getProductIdentifier(), job.getWorkDirectory(), counter));
					taskProcessor.process();
				}
			} else {
				LOGGER.info("[MONITOR] [Step 3] [productName {}] [workDir {}] Executing processes bypassed",
						job.getProductIdentifier(), job.getWorkDirectory());
			}

			activation = devProperties.getStepsActivation().get("upload");
			if (activation == null || activation) {
				LOGGER.info("[MONITOR] [Step 4] [productName {}] [workDir {}] Processing l0 outputs",
						job.getProductIdentifier(), job.getWorkDirectory());
				OutputProcessor outputProcessor = new OutputProcessor(this.s3Factory, this.outputProcuderFactory,
						job.getWorkDirectory(), job.getOutputs(), this.outputListFile,
						this.sizeS3UploadBatch, String.format("[MONITOR] [Step 4] [productName %s] [workDir %s]",
								job.getProductIdentifier(), job.getWorkDirectory()));
				outputProcessor.processOutput();
			} else {
				LOGGER.info("[MONITOR] [Step 4] [productName {}] [workDir {}] Processing l0 outputs bypasssed",
						job.getProductIdentifier(), job.getWorkDirectory());
			}

		} catch (InputDownloaderException e) {
			LOGGER.error("[MONITOR] [productName {}] [workDir {}] Problem during creating working directory : {}",
					job.getProductIdentifier(), job.getWorkDirectory(), e.getMessage());
			this.appStatus.setError();
		} catch (ProcessExecutionException e) {
			LOGGER.error("[MONITOR] [productName {}] [workDir {}] Problem during executing tasks : {}",
					job.getProductIdentifier(), job.getWorkDirectory(), e.getMessage());
			this.appStatus.setError();
		} catch (IllegalArgumentException | ObjectStorageException | IOException e) {
			LOGGER.error("[MONITOR] [productName {}] [workDir {}] Problem during processing outputs : {}",
					job.getProductIdentifier(), job.getWorkDirectory(), e.getMessage());
			this.appStatus.setError();
		} finally {
			activation = devProperties.getStepsActivation().get("erasing");
			if (activation == null || activation) {
				try {
					LOGGER.info("[MONITOR] [Step 5] [productName {}] [workDir {}] Erasing local working directory",
							job.getProductIdentifier(), job.getWorkDirectory());
					delete(job.getWorkDirectory());
				} catch (IOException e) {
					LOGGER.error("[MONITOR] [productName {}] [workDir {}] Failed to erase local working directory",
							job.getProductIdentifier(), job.getWorkDirectory(), e.getMessage());
					this.appStatus.setError();
				}
			} else {
				LOGGER.info("[MONITOR] [Step 5] [productName {}] [workDir {}] Erasing local working directory bypassed",
						job.getProductIdentifier(), job.getWorkDirectory());
			}

			LOGGER.info("[MONITOR] [Step 6] [productName {}] [workDir {}] Resuming consumer",
					job.getProductIdentifier(), job.getWorkDirectory());
			if (this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaListenerContainerId) != null) {
				this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaListenerContainerId).resume();
			} else {
				LOGGER.info(
						"[MONITOR] [Step 6] [productName {}] [workDir {}] Cannot resume consumer because no listener {}",
						job.getProductIdentifier(), job.getWorkDirectory(), this.kafkaListenerContainerId);
			}

			LOGGER.info("[MONITOR] [Step 7] [productName {}] [workDir {}] Checking status consumer",
					job.getProductIdentifier(), job.getWorkDirectory());
			if (this.appStatus.getStatus().isStopping()) {
				System.exit(0);
			} else if (this.appStatus.getStatus().isFatalError()) {
				System.exit(-1);
			} else {
				this.appStatus.setWaiting();
			}
		}

		LOGGER.info("[MONITOR] [Step 0] [productName {}] End L0 job generation", job.getProductIdentifier());

		return null;
	}

	private void delete(String path) throws IOException {
		Path p = Paths.get(path);
		Files.walk(p, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
				.peek(System.out::println).forEach(File::delete);
	}

}
