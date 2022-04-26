package esa.s1pdgs.cpoc.compression.worker.file;

import java.io.File;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
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

	private final AbstractMessage event;
	
	private final ProductFamily outputProductFamily;

	/**
	 * OBS service
	 */
	private final ObsClient obsClient;
	

	public FileUploader(final ObsClient obsClient, final String workingDir, final AbstractMessage event, ProductFamily outputProductFamily) {
		this.obsClient = obsClient;
		this.workingDir = workingDir;
		this.event = event;
		this.outputProductFamily = outputProductFamily;
	}
	
	public void processOutput(final ReportingFactory reportingFactory) throws AbstractCodedException, ObsEmptyFileException {
		String outputFileName;
		if (!CompressionEventUtil.isCompressed(event.getKeyObjectStorage())) {
			// Compression
			outputFileName = CompressionEventUtil.composeCompressedKeyObjectStorage(event.getKeyObjectStorage());
		} else {
			// Uncompression
			outputFileName = CompressionEventUtil.removeZipFromKeyObjectStorage(event.getKeyObjectStorage());
		}
		
		final File productPath = new File(workingDir + "/" + outputFileName + "/" + outputFileName);
		if (!productPath.exists()) {
			throw new InternalErrorException(
					"Operation aborted: The compressed product " + productPath + " does not exist");
		}
					
		LOGGER.info("Uploading compressed product {} [{}]", productPath, event.getProductFamily());
		final FileObsUploadObject uploadObject = new FileObsUploadObject(outputProductFamily, outputFileName, productPath);
		
		// upload
		if (Thread.currentThread().isInterrupted()) {
			throw new InternalErrorException("The current thread as been interrupted");
		}
		obsClient.upload(Collections.singletonList(new FileObsUploadObject(uploadObject.getFamily(), uploadObject.getKey(), uploadObject.getFile())), reportingFactory);
	}

}
