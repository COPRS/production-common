package fr.viveris.s1pdgs.level0.wrapper.services.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobInputDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ApplicationLevel;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsS3Exception;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;
import fr.viveris.s1pdgs.level0.wrapper.services.task.PoolExecutorCallable;
import fr.viveris.s1pdgs.level0.wrapper.utils.FileUtils;

/**
 * Class which create the local working directory and download all the inputs
 * files
 * 
 * @author Olivier Bex-Chauvet
 *
 */
public class InputDownloader {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(InputDownloader.class);

	/**
	 * Factory for accessing to the object storage
	 */
	private final S3Factory s3Factory;

	/**
	 * Path to the local working directory
	 */
	private final String localWorkingDir;

	/**
	 * List of all the inputs
	 */
	private final List<JobInputDto> inputs;

	private final int sizeS3DownloadBatch;

	private String prefixMonitorLogs;

	private final PoolExecutorCallable poolProcessorExecutor;

	private final ApplicationLevel appLevel;

	/**
	 * Constructor
	 * 
	 * @param localWorkingDir
	 * @param inputs
	 */
	public InputDownloader(final S3Factory s3Factory, String localWorkingDir, List<JobInputDto> inputs,
			final int sizeS3DownloadBatch, final PoolExecutorCallable poolProcessorExecutor,
			final ApplicationLevel appLevel) {
		this.s3Factory = s3Factory;
		this.localWorkingDir = localWorkingDir;
		this.inputs = inputs;
		this.sizeS3DownloadBatch = sizeS3DownloadBatch;
		this.prefixMonitorLogs = "[MONITOR] [Step 2]";
		this.poolProcessorExecutor = poolProcessorExecutor;
		this.appLevel = appLevel;
	}

	/**
	 * Constructor
	 * 
	 * @param localWorkingDir
	 * @param inputs
	 */
	public InputDownloader(final S3Factory s3Factory, String localWorkingDir, List<JobInputDto> inputs,
			final int sizeS3DownloadBatch, String prefixMonitorLogs, PoolExecutorCallable poolProcessorExecutor,
			final ApplicationLevel appLevel) {
		this(s3Factory, localWorkingDir, inputs, sizeS3DownloadBatch, poolProcessorExecutor, appLevel);
		this.prefixMonitorLogs = prefixMonitorLogs;
	}

	/**
	 * Function which create the local working directory and download all the input
	 * files into it
	 * 
	 * @throws InputDownloaderException
	 * 
	 * @throws IOException
	 * @throws ObsS3Exception
	 */
	public void processInputs() throws CodedException {

		List<S3DownloadFile> downloadToBatch = new ArrayList<>();
		
		// Create working directory
		LOGGER.info("{} 1 - Creating working directory", this.prefixMonitorLogs);
		File workingDir = new File(this.localWorkingDir);
		workingDir.mkdirs();

		// Create Status.txt file with ONGOING
		LOGGER.info("{} 2 - Creating status.txt file with ONGOING", this.prefixMonitorLogs);
		File statusFile = new File(this.localWorkingDir + "Status.txt");
		try {
			FileUtils.writeFile(statusFile, "ONGOING");
		} catch (IOException e) {
			throw new InternalErrorException("Cannot write file " + this.localWorkingDir + "Status.txt");
		}

		// Create necessary directories and download input with content in message
		LOGGER.info("{} 3 - Starting organizing inputs", this.prefixMonitorLogs);
		for (JobInputDto input : inputs) {
			// Check if a directory shall be created
			File parent = (new File(input.getLocalPath())).getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			// Upload input if in message else wait to list all input and download them from
			// object storage per batch
			switch (input.getFamily()) {
			case "JOB":
				LOGGER.info("Job order will be stored in {}", input.getLocalPath());
				File jobOrber = new File(input.getLocalPath());
				try {
					FileUtils.writeFile(jobOrber, input.getContentRef());
				} catch (IOException e) {
					throw new InternalErrorException("Cannot write file " + input.getLocalPath());
				}
				break;
			case "RAW":
			case "CONFIG":
			case "L0_PRODUCT":
			case "L0_ACN":
				LOGGER.info("Input {}-{} will be stored in {}", input.getFamily(), input.getContentRef(),
						input.getLocalPath());
				downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(input.getFamily()),
						input.getContentRef(), input.getLocalPath()));
				break;
			case "BLANK":
				LOGGER.info("Input {} will be ignored", input.getContentRef());
				break;
			default:
				throw new UnknownFamilyException("Family not managed in input downloader ", input.getFamily());
			}

		}

		// Download input from object storage in batch
		LOGGER.info("{} 4 - Starting downloading inputs from object storage", this.prefixMonitorLogs);
		double size = (Integer.valueOf(downloadToBatch.size())).doubleValue();
		double nbPool = Math.ceil(size / this.sizeS3DownloadBatch);
		int nbUploadedRaw = 0;
		for (int i = 0; i < nbPool; i++) {
			if (!Thread.currentThread().isInterrupted()) {
				LOGGER.info("{} 4 - Starting downloading batch {}", this.prefixMonitorLogs, i);
				int lastIndex = Math.min((i + 1) * this.sizeS3DownloadBatch, downloadToBatch.size());
				List<S3DownloadFile> subListS3 = downloadToBatch.subList(i * this.sizeS3DownloadBatch, lastIndex);
				this.s3Factory.downloadFilesPerBatch(subListS3);
				switch (appLevel) {
				case L0:
					if (nbUploadedRaw < 2) {
						for (S3DownloadFile s3DownloadFile : subListS3) {
							if (s3DownloadFile.getFamily() == ProductFamily.RAW) {
								nbUploadedRaw++;
							}
						}
						if (nbUploadedRaw >= 2) {
							// On suppose l'ordre de traitement des input: les 2 preemiers RAW sont le raw1
							// du channel 1 et le raw 1 du channel 2
							LOGGER.info("{} 4 - Setting process executor as active", this.prefixMonitorLogs);
							this.poolProcessorExecutor.setActive(true);
						}
					}
					break;
				default:
					// nothing
					break;
				}
			} else {
				throw new InternalErrorException("The current thread as been interrupted");
			}
		}

		// Add status file
		LOGGER.info("{} 5 - Updating status.txt file with COMPLETED", this.prefixMonitorLogs);
		try {
			FileUtils.writeFile(statusFile, "COMPLETED");
			poolProcessorExecutor.setActive(true);
		} catch (IOException e) {
			throw new InternalErrorException("Cannot write file " + this.localWorkingDir + "Status.txt");
		}
	}
}
