package esa.s1pdgs.cpoc.compression.worker.file;

import java.io.File;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

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

	private final CatalogEvent catalogEvent;


	public FileDownloader(final ObsClient obsClient, final String localWorkingDir, final CatalogEvent catalogEvent) {
		this.obsClient = obsClient;
		this.localWorkingDir = localWorkingDir;
		this.catalogEvent = catalogEvent;
	}

	/**
	 * Prepare the working directory by downloading all needed inputs
	 * 
	 * @throws AbstractCodedException
	 */
	public void processInputs(final ReportingFactory reportingFactory) throws AbstractCodedException {
		// prepare directory structure
		LOGGER.info("CompressionProcessor 1 - Creating working directory");
		final File workingDir = new File(localWorkingDir);
		workingDir.mkdirs();

		// organize inputs
		final ObsDownloadObject inputProduct = buildInput();

		// download input from object storage in batch
		LOGGER.info("4 - Starting downloading input product {}", inputProduct);
		obsClient.download(Arrays.asList(new ObsDownloadObject(inputProduct.getFamily(), inputProduct.getKey(), inputProduct.getTargetDir())), reportingFactory);
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
		LOGGER.info("CompressionProcessor 3 - Starting organizing inputs");
		
		if (catalogEvent.getKeyObjectStorage() == null) {
			throw new InternalErrorException("productName to download cannot be null");
		}

		final String targetFile = this.localWorkingDir+"/"+CompressionEventUtil.composeCompressedKeyObjectStorage(catalogEvent.getKeyObjectStorage());
		LOGGER.info("Input {} will be stored in {}", catalogEvent.getKeyObjectStorage(), targetFile);
		return new ObsDownloadObject(catalogEvent.getProductFamily(), catalogEvent.getKeyObjectStorage(), targetFile);

	}
}
