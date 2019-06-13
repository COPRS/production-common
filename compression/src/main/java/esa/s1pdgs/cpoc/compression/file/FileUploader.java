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
import esa.s1pdgs.cpoc.compression.model.mqi.ObsQueueMessage;
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

		 List<ObsQueueMessage> outputToPublish = new ArrayList<>();

		try {
    		 File workDir = new File(workingDir+"/"+job.getInput().getLocalPath());
    		 ProductFamily productFamily = getCompressedProductFamily(job.getFamily());
			S3UploadFile uploadFile = new S3UploadFile(productFamily, "xxx", workDir);
//// 			// Upload per batch the output
			processProducts(reportingFactory, uploadFile, outputToPublish);

// 	        reporting.reportStopWithTransfer("End handling of output {} " + listoutputs, size);
		} catch (AbstractCodedException e) {
			reporting.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
			throw e;
		}
	}
	
	ProductFamily getCompressedProductFamily(ProductFamily inputFamily) {
        return ProductFamily.fromValue(job.getFamily().toString()+"_zip");
	}

	final void processProducts(final Reporting.Factory reportingFactory, final S3UploadFile uploadFile,
			final List<ObsQueueMessage> outputToPublish) throws AbstractCodedException {

		final Reporting report = reportingFactory.product(null, null).newReporting(2);
		try {
			report.reportStart("Start uploading compressed product" + uploadFile);

			if (Thread.currentThread().isInterrupted()) {
				throw new InternalErrorException("The current thread as been interrupted");
			}
			this.obsService.uploadFilesPerBatch(Collections.singletonList(uploadFile));
			report.reportStop("End uploading compressed product " + uploadFile);

		} catch (AbstractCodedException e) {
			report.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
			throw e;
		}
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
			final List<ObsQueueMessage> outputToPublish) throws AbstractCodedException {

//        LOGGER.info("{} 3 - Publishing KAFKA messages for batch {}",
//                prefixMonitorLogs, nbBatch);
		Iterator<ObsQueueMessage> iter = outputToPublish.iterator();
		boolean stop = false;
		while (!stop && iter.hasNext()) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InternalErrorException("The current thread as been interrupted");
			}
			ObsQueueMessage msg = iter.next();
			if (nextKeyUpload.startsWith(msg.getKeyObs())) {
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
