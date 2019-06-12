package esa.s1pdgs.cpoc.compression.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.compression.model.obs.S3DownloadFile;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.compression.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
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
	private final LevelJobInputDto input;

	/**
	 * Batch size for downloading inputs from OBS
	 */
	private final int sizeDownBatch;

	/**
	 * Prefix to concatene to monitor logs
	 */
	private final String prefixMonitorLogs;

	/**
	 * Executor which executes processes. Shall be informed when all inputs are
	 * download
	 */
	private final PoolExecutorCallable poolProcExecutor;

	public FileDownloader(final ObsService obsService, final String localWorkingDir,
			final LevelJobInputDto input, final int sizeDownBatch, final String prefixMonitorLogs,
			final PoolExecutorCallable poolProcExecutor) {
		this.obsService = obsService;
		this.localWorkingDir = localWorkingDir;
		this.input = input;
		this.sizeDownBatch = sizeDownBatch;
		this.poolProcExecutor = poolProcExecutor;
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
		List<S3DownloadFile> downloadToBatch = sortInputs();

		final Reporting reporting = new LoggerReporting.Factory(LOGGER, "InputDownloader").newReporting(0);

		final StringBuilder stringBuilder = new StringBuilder();
		for (final S3DownloadFile input : downloadToBatch) {
			stringBuilder.append(input.getKey()).append(' ');
		}
		final String listinputs = stringBuilder.toString().trim();

		reporting.reportStart("Start download of products " + listinputs);

		// Download input from object storage in batch
		try {
			downloadInputs(downloadToBatch);
			reporting.reportStopWithTransfer("End download of products " + listinputs, getWorkdirSize());
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
	protected List<S3DownloadFile> sortInputs() throws InternalErrorException, UnknownFamilyException {
		LOGGER.info("{} 3 - Starting organizing inputs", prefixMonitorLogs);

		List<S3DownloadFile> downloadToBatch = new ArrayList<>();
		
		LOGGER.info("Input {}-{} will be stored in {}", input.getFamily(), input.getContentRef(),
				input.getLocalPath());
		downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(input.getFamily()),
				input.getContentRef(), (new File(input.getLocalPath()).getParent())));
		
		/*
		for (LevelJobInputDto input : inputs) {
			// Check if a directory shall be created
			File parent = (new File(input.getLocalPath())).getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			// Upload input if in message else wait to list all input and
			// download them from
			// object storage per batch
			switch (input.getFamily()) {
			case "JOB_ORDER":
				LOGGER.info("Input {} will be ignored", input.getContentRef());
				break;
			case "EDRS_SESSION":
			case "AUXILIARY_FILE":
			case "L0_SLICE":
			case "L0_ACN":
			case "L0_SEGMENT":
			case "L1_SLICE":
			case "L1_ACN":
			case "L2_ACN":
			case "L2_SLICE":
				LOGGER.info("Input {}-{} will be stored in {}", input.getFamily(), input.getContentRef(),
						input.getLocalPath());
				downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(input.getFamily()),
						input.getContentRef(), (new File(input.getLocalPath()).getParent())));
				break;
			case "BLANK":
				LOGGER.info("Input {} will be ignored", input.getContentRef());
				break;
			default:
				throw new UnknownFamilyException("Family not managed in input downloader ", input.getFamily());
			}

		}*/
		return downloadToBatch;
	}

	/**
	 * Download input from OBS per batch. If we have download 2 raw, the processor
	 * executor can start launch proceses
	 * 
	 * @param downloadToBatch
	 * @throws AbstractCodedException
	 */
	private final void downloadInputs(final List<S3DownloadFile> downloadToBatch) throws AbstractCodedException {

		final int numberOfBatches = (int) Math.ceil(((double) downloadToBatch.size()) / ((double) sizeDownBatch));

		int nbUploadedRaw = 0;
		for (int i = 0; i < numberOfBatches; i++) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InternalErrorException("The current thread as been interrupted");
			} else {
				LOGGER.info("{} 4 - Starting downloading batch {}", prefixMonitorLogs, i);
				int lastIndex = Math.min((i + 1) * sizeDownBatch, downloadToBatch.size());
				List<S3DownloadFile> subListS3 = downloadToBatch.subList(i * sizeDownBatch, lastIndex);
				this.obsService.downloadFilesPerBatch(subListS3);

				nbUploadedRaw += subListS3.stream().filter(file -> file.getFamily() == ProductFamily.EDRS_SESSION)
						.count();
				if (nbUploadedRaw >= 2) {
					// On suppose l'ordre de traitement des input:
					// les 2 preemiers RAW sont le raw1
					// du channel 1 et le raw 1 du channel 2
					LOGGER.info("{} 4 - Setting process executor as active", prefixMonitorLogs);
					poolProcExecutor.setActive(true);
				}

			}
		}
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
