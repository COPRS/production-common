package esa.s1pdgs.cpoc.compression.worker.file;

import java.io.File;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class FileUploader {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(FileUploader.class);

	private final String workingDir;

	private final CatalogEvent catalogEvent;

	/**
	 * OBS service
	 */
	private final ObsClient obsClient;
	

	public FileUploader(final ObsClient obsClient, final String workingDir, final CatalogEvent catalogEvent) {
		this.obsClient = obsClient;
		this.workingDir = workingDir;
		this.catalogEvent = catalogEvent;
	}
	
	public void processOutput(final ReportingFactory reportingFactory) throws AbstractCodedException, ObsEmptyFileException {

		final String outputFileName = CompressionEventUtil.composeCompressedKeyObjectStorage(catalogEvent.getKeyObjectStorage());
		final File productPath = new File(workingDir + "/" + outputFileName + "/" + outputFileName);
		if (!productPath.exists()) {
			throw new InternalErrorException(
					"Operation aborted: The compressed product " + productPath + " does not exist");
		}
					
		LOGGER.info("Uploading compressed product {} [{}]", productPath, catalogEvent.getProductFamily());
		final ProductFamily outputProductFamily = CompressionEventUtil.composeCompressedProductFamily(catalogEvent.getProductFamily());
		final FileObsUploadObject uploadObject = new FileObsUploadObject(outputProductFamily, outputFileName, productPath);
		
		// upload
		if (Thread.currentThread().isInterrupted()) {
			throw new InternalErrorException("The current thread as been interrupted");
		}
		obsClient.upload(Collections.singletonList(new FileObsUploadObject(uploadObject.getFamily(), uploadObject.getKey(), uploadObject.getFile())), reportingFactory);
	}

}
