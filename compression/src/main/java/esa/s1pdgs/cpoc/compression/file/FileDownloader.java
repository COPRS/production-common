package esa.s1pdgs.cpoc.compression.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.compression.model.obs.S3DownloadFile;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

@Service
public class FileDownloader {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(FileDownloader.class);

	/**
	 * Factory for accessing to the object storage
	 */
	private final ObsService obsService;

	/**
	 * Path to the local working directory
	 */
	private final String localWorkingDir;

	/**
	 * List of all the inputs
	 */
	private final CompressionJobDto job;

	/**
	 * Prefix to concatene to monitor logs
	 */
	private final String prefixMonitorLogs;

	public FileDownloader(final ObsService obsService, final String localWorkingDir, final CompressionJobDto job,
			final int sizeDownBatch, final String prefixMonitorLogs) {
		this.obsService = obsService;
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
		S3DownloadFile inputProduct = buildInput();

		final Reporting reporting = new LoggerReporting.Factory(LOGGER, "FileDownloader").newReporting(0);

		reporting.reportStart("Start download of product to compress {} " + inputProduct);

		// Download input from object storage in batch
		try {
			downloadInputs(inputProduct);
			reporting.reportStopWithTransfer("End download of products " + inputProduct, getWorkdirSize());
		} catch (AbstractCodedException e) {
			reporting.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
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
	protected S3DownloadFile buildInput() throws InternalErrorException, UnknownFamilyException {
		LOGGER.info("{} 3 - Starting organizing inputs", prefixMonitorLogs);

		LOGGER.info("Input {}-{} will be stored in {}", job.getFamily(), job.getInput().getContentRef(), job.getInput().getLocalPath());

		return new S3DownloadFile(ProductFamily.fromValue(job.getInput().getFamily()), job.getInput().getContentRef(),
				(new File(job.getInput().getLocalPath()).getParent()));

	}

	/**
	 * Download input from OBS per batch. If we have download 2 raw, the processor
	 * executor can start launch proceses
	 * 
	 * @param inputProduct
	 * @throws AbstractCodedException
	 */
	private final void downloadInputs(final S3DownloadFile inputProduct) throws AbstractCodedException {
		LOGGER.info("4 - Starting downloading input product {}", inputProduct);
		this.obsService.downloadFilesPerBatch(Collections.singletonList(inputProduct));
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
