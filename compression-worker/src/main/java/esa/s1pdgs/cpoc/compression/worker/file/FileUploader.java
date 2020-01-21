package esa.s1pdgs.cpoc.compression.worker.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.compression.worker.model.mqi.CompressedProductQueueMessage;
import esa.s1pdgs.cpoc.compression.worker.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

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

	public FileUploader(final ObsClient obsClient, final OutputProducerFactory producerFactory,
			final String workingDir, final GenericMessageDto<CompressionJob> inputMessage,
			final CompressionJob job) {
		this.obsClient = obsClient;
		this.producerFactory = producerFactory;
		this.workingDir = workingDir;
		this.inputMessage = inputMessage;
		this.job = job;
	}

	public String processOutput(final Reporting.ChildFactory reportingChildFactory) throws AbstractCodedException, ObsEmptyFileException {

		final List<CompressedProductQueueMessage> outputToPublish = new ArrayList<>();

		String zipFileName = job.getOutputKeyObjectStorage();
		final File productPath = new File(workingDir + "/" + zipFileName + "/" + zipFileName);
		if (!productPath.exists()) {
			throw new InternalErrorException(
					"Operation aborted: The compressed product " + productPath + " does not exist");
		}
					
		LOGGER.info("Uploading compressed product {} [{}]",productPath, job.getProductFamily());
		final ProductFamily zipProductFamily = job.getOutputProductFamily();
		final ObsUploadObject uploadObject = new ObsUploadObject(zipProductFamily, zipFileName, productPath);
		
		final CompressedProductQueueMessage cpqm = new CompressedProductQueueMessage(zipProductFamily, zipFileName, zipFileName);
		outputToPublish.add(cpqm);

		// upload
		if (Thread.currentThread().isInterrupted()) {
			throw new InternalErrorException("The current thread as been interrupted");
		}
		obsClient.upload(Arrays.asList(new ObsUploadObject(uploadObject.getFamily(), uploadObject.getKey(), uploadObject.getFile())), reportingChildFactory);
		
		publishAccordingUploadFiles(reportingChildFactory, NOT_KEY_OBS, outputToPublish);
        
        return zipFileName;
	}

	/**
	 * Public uploaded files, i.e. unitl the output to publish is the next key to
	 * upload
	 * 
	 * @param nextKeyUpload
	 * @param outputToPublish
	 * @throws AbstractCodedException
	 */
	private void publishAccordingUploadFiles(final Reporting.ChildFactory reportingChildFactory, final String nextKeyUpload,
			final List<CompressedProductQueueMessage> outputToPublish) throws AbstractCodedException {

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
				final Reporting report = reportingChildFactory.newChild("Publish");

				report.begin(new ReportingMessage("Start publishing file {}", msg.getObjectStorageKey()));
				try {
					producerFactory.sendOutput(msg, inputMessage);
					report.end(new ReportingMessage("End publishing file {}", msg.getObjectStorageKey()));
				} catch (final MqiPublicationError ace) {
					report.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));
				}
				iter.remove();
			}

		}
	}

}
