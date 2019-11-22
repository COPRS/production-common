package esa.s1pdgs.cpoc.compression.worker.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public class FileDownloader {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(FileDownloader.class);

	/**
	 * Factory for accessing to the object storage
	 */
	private final ObsClient obsClient;

	/**
	 * Path to the local working directory
	 */
	private final String localWorkingDir;

	/**
	 * List of all the inputs
	 */
	private final CompressionJob job;

	/**
	 * Prefix to concatene to monitor logs
	 */
	private final String prefixMonitorLogs;

	public FileDownloader(final ObsClient obsClient, final String localWorkingDir, final CompressionJob job,
			final int sizeDownBatch, final String prefixMonitorLogs) {
		this.obsClient = obsClient;
		this.localWorkingDir = localWorkingDir;
		this.job = job;
		this.prefixMonitorLogs = prefixMonitorLogs;
	}

	/**
	 * Prepare the working directory by downloading all needed inputs
	 * 
	 * @throws AbstractCodedException
	 */
	public void processInputs() throws AbstractCodedException {

		// Initialize
		initializeDownload();

		// Create necessary directories and download input with content in
		// message
		ObsDownloadObject inputProduct = buildInput();

		final Reporting reporting = new LoggerReporting.Factory("FileDownloader").newReporting(0);

		reporting.begin(new ReportingMessage("Start download of product to compress {}", inputProduct));

		// Download input from object storage in batch
		try {
			downloadInputs(inputProduct);
			reporting.end(new ReportingMessage(getWorkdirSize(), "End download of products {}", inputProduct));
		} catch (AbstractCodedException e) {
			reporting.error(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
			throw e;
		}
	}

	/**
	 * Create the working directory and the status file
	 * 
	 * @throws InternalErrorException
	 */
	private void initializeDownload() throws InternalErrorException {
		LOGGER.info("{} 1 - Creating working directory", prefixMonitorLogs);
		File workingDir = new File(localWorkingDir);
		workingDir.mkdirs();
	}

	/**
	 * Sort inputs: - if JOB => create the file - if RAW / CONFIG / L0_PRODUCT /
	 * L0_ACN => convert into S3DownloadFile - if BLANK => ignore - else => throw
	 * exception
	 * 
	 * @return
	 * @throws InternalErrorException
	 * @throws UnknownFamilyException
	 */
	protected ObsDownloadObject buildInput() throws InternalErrorException, UnknownFamilyException {
		LOGGER.info("{} 3 - Starting organizing inputs", prefixMonitorLogs);
		
		if (job.getInputKeyObjectStorage() == null) {
			throw new InternalErrorException("productName to download cannot be null");
		}

		String targetFile = this.localWorkingDir+"/"+job.getOutputKeyObjectStorage();
		LOGGER.info("Input {} will be stored in {}", job.getInputKeyObjectStorage(), targetFile);
		return new ObsDownloadObject(job.getProductFamily(), job.getInputKeyObjectStorage(),targetFile);

	}

	/**
	 * Download input from OBS per batch. If we have download 2 raw, the processor
	 * executor can start launch proceses
	 * 
	 * @param inputProduct
	 * @throws AbstractCodedException
	 */
	private final void downloadInputs(final ObsDownloadObject inputProduct) throws AbstractCodedException {
		LOGGER.info("4 - Starting downloading input product {}", inputProduct);
		this.obsClient.download(Arrays.asList(new ObsDownloadObject(inputProduct.getFamily(), inputProduct.getKey(), inputProduct.getTargetDir())));
	}

	private final long getWorkdirSize() throws InternalErrorException {
		try {
			final Path folder = Paths.get(localWorkingDir);
			return Files.walk(folder).filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();

		} catch (IOException e) {
			throw new InternalErrorException(
					String.format("Error on determining size of %s: %s", localWorkingDir, e.getMessage()), e);
		}
	}
}
