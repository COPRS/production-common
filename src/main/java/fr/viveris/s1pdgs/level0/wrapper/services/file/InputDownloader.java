package fr.viveris.s1pdgs.level0.wrapper.services.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobInputDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InputDownloaderException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(InputDownloader.class);

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

	/**
	 * Constructor
	 * 
	 * @param localWorkingDir
	 * @param inputs
	 */
	public InputDownloader(final S3Factory s3Factory, String localWorkingDir, List<JobInputDto> inputs,
			final int sizeS3DownloadBatch) {
		this.s3Factory = s3Factory;
		this.localWorkingDir = localWorkingDir;
		this.inputs = inputs;
		this.sizeS3DownloadBatch = sizeS3DownloadBatch;
		this.prefixMonitorLogs = "[MONITOR] [Step 2]";
	}

	/**
	 * Constructor
	 * 
	 * @param localWorkingDir
	 * @param inputs
	 */
	public InputDownloader(final S3Factory s3Factory, String localWorkingDir, List<JobInputDto> inputs,
			final int sizeS3DownloadBatch, String prefixMonitorLogs) {
		this(s3Factory, localWorkingDir, inputs, sizeS3DownloadBatch);
		this.prefixMonitorLogs = prefixMonitorLogs;
	}

	/**
	 * Function which create the local working directory and download all the input
	 * files into it
	 * 
	 * @throws InputDownloaderException
	 * 
	 * @throws IOException
	 * @throws ObjectStorageException
	 */
	public void processInputs() throws InputDownloaderException {

		List<S3DownloadFile> downloadToBatch = new ArrayList<>();

		// Create necessary directories and download input with content in message
		LOGGER.info("{} 1 - Starting organizing inputs", this.prefixMonitorLogs);
		for (JobInputDto input : inputs) {
			try {
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
					writeFile(jobOrber, input.getContentRef());
					break;
				case "RAW":
				case "CONFIG":
				case "L0_PRODUCT":
				case "L0_ACN":
					LOGGER.info("Input {}-{} will be stored in {}", input.getFamily(), input.getContentRef(), input.getLocalPath());
					downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(input.getFamily()),
							input.getContentRef(), input.getLocalPath()));
					break;
				case "BLANK":
					LOGGER.info("Input {} will be ignored", input.getContentRef());
					break;
				default:
					throw new IllegalArgumentException("Family not managed in input downloader " + input.getFamily());
				}
			} catch (IllegalArgumentException | IOException e) {
				throw new InputDownloaderException(e.getMessage(), input.getFamily(), input.getLocalPath());
			}
		}

		// Download input from object storage in batch
		LOGGER.info("{} 2 - Starting downloading inputs from object storage", this.prefixMonitorLogs);
		try {
			double size = (Integer.valueOf(downloadToBatch.size())).doubleValue();
			double nbPool = Math.ceil(size / this.sizeS3DownloadBatch);
			for (int i = 0; i < nbPool; i++) {
				LOGGER.info("{} 2 - Starting downloading batch {}", this.prefixMonitorLogs, i);
				int lastIndex = Math.min((i + 1) * this.sizeS3DownloadBatch, downloadToBatch.size());
				this.s3Factory.downloadFilesPerBatch(downloadToBatch.subList(i * this.sizeS3DownloadBatch, lastIndex));
			}
		} catch (ObjectStorageException e) {
			throw new InputDownloaderException(e.getMessage());
		}

		// Add status file
		LOGGER.info("{} 3 - Creating status.txt file", this.prefixMonitorLogs);
		File statusFile = new File(this.localWorkingDir + "Status.txt");
		try {
			writeFile(statusFile, "COMPLETED");
		} catch (IOException e) {
			throw new InputDownloaderException(e.getMessage(), "N/A", this.localWorkingDir + "Status.txt");
		}
	}

	/**
	 * Function which write data in file
	 * 
	 * @param fileToComplete
	 * @param data
	 * @throws IOException
	 */
	private void writeFile(File fileToComplete, String data) throws IOException {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter = new FileWriter(fileToComplete);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(data);
			bufferedWriter.flush();
			bufferedWriter.close();
			fileWriter.close();
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		}

	}
}
