package esa.s1pdgs.cpoc.compression.worker.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.compression.worker.model.mqi.CompressedProductQueueMessage;
import esa.s1pdgs.cpoc.compression.worker.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
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

	private final CompressionJob job;

	/**
	 * Output producer factory for message queue system
	 */
	private final OutputProducerFactory producerFactory;

	/**
	 * Input message
	 */
	private final GenericMessageDto<CompressionJob> inputMessage;

	/**
	 * Cannot be a key in obs
	 */
	protected static final String NOT_KEY_OBS = "IT_IS_NOT_A_KEY";


	/**
	 * OBS service
	 */
	private final ObsClient obsClient;
	
	private final UUID reportingUuid;

	public FileUploader(final ObsClient obsClient, final OutputProducerFactory producerFactory,
			final String workingDir, final GenericMessageDto<CompressionJob> inputMessage,
			final CompressionJob job, final UUID reportingUuid) {
		this.obsClient = obsClient;
		this.producerFactory = producerFactory;
		this.workingDir = workingDir;
		this.inputMessage = inputMessage;
		this.job = job;
		this.reportingUuid = reportingUuid;
	}
	
	public List<GenericPublicationMessageDto<CompressionEvent>> processOutput(final ReportingFactory reportingFactory) throws AbstractCodedException, ObsEmptyFileException {
		final List<CompressedProductQueueMessage> outputToPublish = new ArrayList<>();

		final String outputFileName = job.getOutputKeyObjectStorage();
		final File productPath = new File(workingDir + "/" + outputFileName + "/" + outputFileName);
		if (!productPath.exists()) {
			throw new InternalErrorException(
					"Operation aborted: The compressed product " + productPath + " does not exist");
		}
					
		LOGGER.info("Uploading compressed/uncompressed product {} [{}]", productPath, job.getProductFamily());
		final ProductFamily outputProductFamily = job.getOutputProductFamily();
		final FileObsUploadObject uploadObject = new FileObsUploadObject(outputProductFamily, outputFileName, productPath);
		final CompressedProductQueueMessage cpqm = new CompressedProductQueueMessage(outputProductFamily, outputFileName, outputFileName, job.getCompressionDirection());
		outputToPublish.add(cpqm);
		
		// upload
		if (Thread.currentThread().isInterrupted()) {
			throw new InternalErrorException("The current thread as been interrupted");
		}
		obsClient.upload(Arrays.asList(new FileObsUploadObject(uploadObject.getFamily(), uploadObject.getKey(), uploadObject.getFile())), reportingFactory);
        return publishAccordingUploadFiles(NOT_KEY_OBS, outputToPublish);
	}

	/**
	 * Public uploaded files, i.e. unitl the output to publish is the next key to
	 * upload
	 * 
	 * @param nextKeyUpload
	 * @param outputToPublish
	 * @throws AbstractCodedException
	 */
	private List<GenericPublicationMessageDto<CompressionEvent>> publishAccordingUploadFiles(final String nextKeyUpload,
			final List<CompressedProductQueueMessage> outputToPublish) throws AbstractCodedException {
		final List<GenericPublicationMessageDto<CompressionEvent>> result = new ArrayList<>();
        LOGGER.info("{} 3 - Publishing KAFKA messages for batch ");
		final Iterator<CompressedProductQueueMessage> iter = outputToPublish.iterator();
		boolean stop = false;
		while (!stop && iter.hasNext()) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InternalErrorException("The current thread as been interrupted");
			}
			final CompressedProductQueueMessage msg = iter.next();
			if (nextKeyUpload.startsWith(msg.getObjectStorageKey())) {
				stop = true;
			} else {
				result.add(producerFactory.sendOutput(msg, inputMessage, reportingUuid));
				iter.remove();
			}

		}
		return result;
	}

}
