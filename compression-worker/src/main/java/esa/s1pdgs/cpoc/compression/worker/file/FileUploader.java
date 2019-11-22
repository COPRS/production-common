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
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.report.LoggerReporting;
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

	public String processOutput() throws AbstractCodedException {
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("FileUploader");
		final Reporting reporting = reportingFactory.newReporting(0);

		List<CompressedProductQueueMessage> outputToPublish = new ArrayList<>();

		try {
			String zipFileName = job.getOutputKeyObjectStorage();
			File productPath = new File(workingDir + "/" + zipFileName);
			reporting.begin(new ReportingMessage("Start uploading {}", zipFileName));
			if (!productPath.exists()) {
				throw new InternalErrorException(
						"The compressed product " + productPath + " does not exist, stopping upload");
			}
						
			LOGGER.info("Uploading compressed product {} [{}]",productPath, job.getProductFamily());
			ProductFamily zipProductFamily = job.getOutputProductFamily();
			ObsUploadObject uploadObject = new ObsUploadObject(zipProductFamily, zipFileName, productPath);
			
			CompressedProductQueueMessage cpqm = new CompressedProductQueueMessage(zipProductFamily, zipFileName, zipFileName);
			outputToPublish.add(cpqm);
		
//// 			// Upload per batch the output
			processProducts(reportingFactory, uploadObject, outputToPublish);
			
 	        reporting.end(new ReportingMessage(productPath.length(), "End uploading {}", zipFileName));
 	        
 	        return zipFileName;
		} catch (AbstractCodedException e) {
			reporting.error(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
			throw e;
		}
	}

	final void processProducts(final Reporting.Factory reportingFactory, final ObsUploadObject uploadFile,
			final List<CompressedProductQueueMessage> outputToPublish) throws AbstractCodedException {

		if (Thread.currentThread().isInterrupted()) {
			throw new InternalErrorException("The current thread as been interrupted");
		}
		obsClient.upload(Arrays.asList(new ObsUploadObject(uploadFile.getFamily(), uploadFile.getKey(), uploadFile.getFile())));


		publishAccordingUploadFiles(reportingFactory, NOT_KEY_OBS, outputToPublish);
	}

	/**
	 * Public uploaded files, i.e. unitl the output to publish is the next key to
	 * upload
	 * 
	 * @param nextKeyUpload
	 * @param outputToPublish
	 * @throws AbstractCodedException
	 */
	private void publishAccordingUploadFiles(final Reporting.Factory reportingFactory, final String nextKeyUpload,
			final List<CompressedProductQueueMessage> outputToPublish) throws AbstractCodedException {

        LOGGER.info("{} 3 - Publishing KAFKA messages for batch ");
		Iterator<CompressedProductQueueMessage> iter = outputToPublish.iterator();
		boolean stop = false;
		while (!stop && iter.hasNext()) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InternalErrorException("The current thread as been interrupted");
			}
			CompressedProductQueueMessage msg = iter.next();
			if (nextKeyUpload.startsWith(msg.getObjectStorageKey())) {
				stop = true;
			} else {
				final Reporting report = reportingFactory.newReporting(1);

				report.begin(new ReportingMessage("Start publishing message"));
				try {
					producerFactory.sendOutput(msg, inputMessage);
					report.end(new ReportingMessage("End publishing message"));
				} catch (MqiPublicationError ace) {
					report.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));
				}
				iter.remove();
			}

		}
	}

}
