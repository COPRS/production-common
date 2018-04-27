package fr.viveris.s1pdgs.level0.wrapper.services.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobOutputDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsS3Exception;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.model.kafka.FileQueueMessage;
import fr.viveris.s1pdgs.level0.wrapper.model.kafka.ObsQueueMessage;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3UploadFile;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class OutputProcessor {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(OutputProcessor.class);

	/**
	 * Amazon S3 service for L0 slices
	 */
	private final S3Factory s3Factory;

	private final OutputProcuderFactory outputProcuderFactory;

	/**
	 * Path of list file
	 */

	private final String workDirectory;

	private final String listFile;

	private final List<JobOutputDto> authorizedOutputs;

	private final int sizeS3UploadBatch;

	private String prefixMonitorLogs;

	public OutputProcessor(final S3Factory s3Factory, final OutputProcuderFactory outputProcuderFactory,
			final String workDirectory, final List<JobOutputDto> authorizedOutputs, final String listFile,
			final int sizeS3UploadBatch) {
		this.s3Factory = s3Factory;
		this.outputProcuderFactory = outputProcuderFactory;
		this.workDirectory = workDirectory;
		this.listFile = listFile;
		this.authorizedOutputs = authorizedOutputs;
		this.sizeS3UploadBatch = sizeS3UploadBatch;
	}

	public OutputProcessor(final S3Factory s3Factory, final OutputProcuderFactory outputProcuderFactory,
			final String workDirectory, final List<JobOutputDto> authorizedOutputs, final String listFile,
			final int sizeS3UploadBatch, String prefixMonitorLogs) {
		this(s3Factory, outputProcuderFactory, workDirectory, authorizedOutputs, listFile, sizeS3UploadBatch);
		this.prefixMonitorLogs = prefixMonitorLogs;
	}

	/**
	 * Function which process all the output of L0 process
	 * 
	 * @throws ObsS3Exception
	 * @throws IOException
	 */
	public void processOutput() throws CodedException {

		LOGGER.info("{} 1 - Starting organizing outputs", this.prefixMonitorLogs);

		List<String> lines = null;
		try {
			lines = Files.lines(Paths.get(this.listFile)).collect(Collectors.toList());
		} catch (IOException ioe) {
			throw new InternalErrorException("Cannot parse result list file " + this.listFile + ": " + ioe.getMessage(),
					ioe);
		}
		List<S3UploadFile> uploadBatch = new ArrayList<>();
		List<ObsQueueMessage> outputToPublish = new ArrayList<>();
		List<FileQueueMessage> reportToPublish = new ArrayList<>();

		for (String line : lines) {

			// Extract the product name and the complete filepath
			// First, remove the first directory (NRT or REPORT)
			String filePath = this.workDirectory + line;
			String productName = line;
			int index = line.indexOf('/');
			if (index != -1) {
				productName = line.substring(index + 1);
			}
			// Second: if file ISIP, retrieve only .SAFE
			if (productName.toUpperCase().endsWith("ISIP")) {
				productName = productName.substring(0, productName.length() - 4) + "SAFE";
				filePath = filePath + "/" + productName;
			}

			// Check if match regular exp
			JobOutputDto matchOutput = null;
			for (JobOutputDto jobOutputDto : authorizedOutputs) {
				if (Pattern.matches(jobOutputDto.getRegexp().substring(this.workDirectory.length()), productName)) {
					matchOutput = jobOutputDto;
					break;
				}
			}

			// If match process output
			if (matchOutput != null) {
				File f = new File(filePath);
				ProductFamily family = ProductFamily.fromValue(matchOutput.getFamily());
				switch (family) {
				case L0_REPORT:
				case L1_REPORT:
					// If report, put in a cache to send report
					LOGGER.info("Output {} is considered as belonging to the family {}", productName,
							matchOutput.getFamily());
					reportToPublish.add(new FileQueueMessage(family, productName, f));
					break;
				case L0_PRODUCT:
				case L0_ACN:
				case L1_PRODUCT:
				case L1_ACN:
					// If compatible object storage, put in a cache to upload per batch
					LOGGER.info("Output {} is considered as belonging to the family {}", productName,
							matchOutput.getFamily());
					uploadBatch.add(new S3UploadFile(family, productName, f));
					outputToPublish.add(new ObsQueueMessage(family, productName, productName));
					break;
				case BLANK:
					LOGGER.info("Output {} will be ignored", productName);
					break;
				default:
					throw new UnknownFamilyException("Family not managed in output processor ", family.name());
				}
			} else {
				LOGGER.warn("Output {} ignored because no found matching regular expression", productName);
			}

		}

		// Upload per batch the output
		LOGGER.info("{} 2 - Starting processing object storage compatible outputs", this.prefixMonitorLogs);
		double size = (Integer.valueOf(uploadBatch.size())).doubleValue();
		double nbPool = Math.ceil(size / this.sizeS3UploadBatch);
		for (int i = 0; i < nbPool; i++) {
			int lastIndex = Math.min((i + 1) * this.sizeS3UploadBatch, uploadBatch.size());
			List<S3UploadFile> sublist = uploadBatch.subList(i * this.sizeS3UploadBatch, lastIndex);

			if (i > 0) {
				int j = i -1;
				LOGGER.info("{} 2 - Publishing KAFKA messages for batch {}", this.prefixMonitorLogs, j);
				Iterator<ObsQueueMessage> iter = outputToPublish.iterator();
				boolean stop = false;
				while (!stop && iter.hasNext()) {
					if (!Thread.currentThread().isInterrupted()) {
						ObsQueueMessage msg = iter.next();
						if (!sublist.get(0).getKey().startsWith(msg.getKeyObs())) {
							LOGGER.info("{} 2 - Publishing KAFKA message for output {}", this.prefixMonitorLogs,
									msg.getProductName());
							this.outputProcuderFactory.sendOutput(msg);
							iter.remove();
						} else {
							stop = true;
						}
					} else {
						throw new InternalErrorException("The current thread as been interrupted");
					}
				}
			}
			LOGGER.info("{} 2 - Uploading batch {} ", this.prefixMonitorLogs, i);
			if (!Thread.currentThread().isInterrupted()) {
				this.s3Factory.uploadFilesPerBatch(sublist);
			} else {
				throw new InternalErrorException("The current thread as been interrupted");
			}
		}
		LOGGER.info("{} 2 - Publishing KAFKA messages for the last batch", this.prefixMonitorLogs);
		Iterator<ObsQueueMessage> iter = outputToPublish.iterator();
		while (iter.hasNext()) {
			if (!Thread.currentThread().isInterrupted()) {
				ObsQueueMessage msg = iter.next();
				LOGGER.info("{} 2 - Publishing KAFKA message for output {}", this.prefixMonitorLogs,
						msg.getProductName());
				this.outputProcuderFactory.sendOutput(msg);
				iter.remove();
			} else {
				throw new InternalErrorException("The current thread as been interrupted");
			}
		}

		// Publish reports
		LOGGER.info("{} 3 - Starting processing not object storage compatible outputs", this.prefixMonitorLogs);
		if (!reportToPublish.isEmpty()) {
			for (FileQueueMessage msg : reportToPublish) {
				if (!Thread.currentThread().isInterrupted()) {
					LOGGER.info("{} 3 - Publishing KAFKA message for output {}", this.prefixMonitorLogs,
							msg.getProductName());
					this.outputProcuderFactory.sendOutput(msg);
				} else {
					throw new InternalErrorException("The current thread as been interrupted");
				}
			}
		}
	}

}
