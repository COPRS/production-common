package esa.s1pdgs.cpoc.compression.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.compression.model.mqi.CompressedProductQueueMessage;
import esa.s1pdgs.cpoc.compression.model.obs.S3UploadFile;
import esa.s1pdgs.cpoc.compression.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public class FileUploader {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(FileUploader.class);

	private final String workingDir;

	private final CompressionJobDto job;

	/**
	 * Output producer factory for message queue system
	 */
	private final OutputProducerFactory producerFactory;

	/**
	 * Input message
	 */
	private final GenericMessageDto<CompressionJobDto> inputMessage;

	/**
	 * Cannot be a key in obs
	 */
	protected static final String NOT_KEY_OBS = "IT_IS_NOT_A_KEY";

	protected static final String SUFFIX_ZIPPRODUCTFAMILY = "_ZIP";

	/**
	 * OBS service
	 */
	private final ObsService obsService;

	public FileUploader(final ObsService obsService, final OutputProducerFactory producerFactory,
			final String workingDir, final GenericMessageDto<CompressionJobDto> inputMessage,
			final CompressionJobDto job) {
		this.obsService = obsService;
		this.producerFactory = producerFactory;
		this.workingDir = workingDir;
		this.inputMessage = inputMessage;
		this.job = job;
	}

	public void processOutput() throws AbstractCodedException {
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "FileUploader");
		final Reporting reporting = reportingFactory.newReporting(0);

		List<CompressedProductQueueMessage> outputToPublish = new ArrayList<>();

		try {
			String zipFileName = job.getProductName() + ".zip";
			File productPath = new File(workingDir + "/" + zipFileName);
			reporting.reportStart("Start uploading " + zipFileName);
			if (!productPath.exists()) {
				throw new InternalErrorException(
						"The compressed product " + productPath + " does not exist, stopping upload");
			}
						
			LOGGER.info("Uploading compressed product {} [{}]",productPath, job.getFamily());
			ProductFamily zipProductFamily = getCompressedProductFamily(job.getFamily());
			S3UploadFile uploadFile = new S3UploadFile(zipProductFamily, zipFileName, productPath);
			
			CompressedProductQueueMessage cpqm = new CompressedProductQueueMessage(zipProductFamily, zipFileName,zipFileName);
			outputToPublish.add(cpqm);
			
//// 			// Upload per batch the output
			processProducts(reportingFactory, uploadFile, outputToPublish);

 	        reporting.reportStopWithTransfer("End uploading " + zipFileName, productPath.length());
		} catch (AbstractCodedException e) {
			reporting.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
			throw e;
		}
	}

	ProductFamily getCompressedProductFamily(ProductFamily inputFamily) {
		return ProductFamily.fromValue(inputFamily.toString() + SUFFIX_ZIPPRODUCTFAMILY);
	}

	final void processProducts(final Reporting.Factory reportingFactory, final S3UploadFile uploadFile,
			final List<CompressedProductQueueMessage> outputToPublish) throws AbstractCodedException {

		if (Thread.currentThread().isInterrupted()) {
			throw new InternalErrorException("The current thread as been interrupted");
		}
		this.obsService.uploadFilesPerBatch(Collections.singletonList(uploadFile));


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
				final Reporting report = reportingFactory.product(null, msg.getProductName()).newReporting(1);

				report.reportStart("Start publishing message");
				try {
					producerFactory.sendOutput(msg, inputMessage);
					report.reportStop("End publishing message");
				} catch (MqiPublicationError ace) {
					report.reportError("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());
				}
				iter.remove();
			}

		}
	}

}
