package esa.s1pdgs.cpoc.compression.worker.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		File productPath;
		String outputFileName;
		if (!CompressionEventUtil.isCompressed(event.getKeyObjectStorage())) {
			// Compression
			outputFileName = CompressionEventUtil.composeCompressedKeyObjectStorage(event.getKeyObjectStorage());
			productPath = new File(workingDir + "/" + outputFileName + "/" + outputFileName);
		} 
		else {
			// Uncompression
			outputFileName = CompressionEventUtil.removeZipFromKeyObjectStorage(event.getKeyObjectStorage());
			productPath = new File(workingDir + "/" + outputFileName + "/" + outputFileName);
			
			// dirty workaround to avoid regressions here. HKTM files are coming in a zip without the SAFE suffix
			// while the directory inside the zip still carries the SAFE suffix. Hence, the previous logic here
			// did not work and to cover this specific scenario, we simply use the directory that has been extracted.
			// To indicate that the workaround is active, it will be logged that this logic tries to detect the 
			// uncompressed file.
			if (!productPath.exists()) {
				final File directory = new File(workingDir + "/" + outputFileName);		
				LOGGER.debug("Searching for uncompressed product in {}", directory);
				final File[] files = directory.listFiles(f -> {
					return !event.getKeyObjectStorage().equals(f.getName());
				});
				
				if (files != null && files.length == 1) {		
					productPath = files[0];
					LOGGER.debug("Found product {}", productPath);
				}		
				else {
					final File[] allFiles = directory.listFiles();
					final List<File> filenames = allFiles != null ? Arrays.asList(allFiles) : Collections.emptyList();					
					LOGGER.warn("Found following unexpected files in directory: {}", filenames);
				}
			}			
		}
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
