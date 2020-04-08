package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.ObsQueueMessage;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc.OQCDefaultTaskFactory;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc.OQCExecutor;
import esa.s1pdgs.cpoc.ipf.execution.worker.service.report.GhostHandlingSegmentReportingOutput;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

/**
 * Process outputs according their family: - publication in message queue system
 * if needed - upload in OBS if needed
 * 
 * @author Viveris Technologies
 */
public class OutputProcessor {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(OutputProcessor.class);

	/**
	 * Cannot be a key in obs
	 */
	protected static final String NOT_KEY_OBS = "IT_IS_NOT_A_KEY";

	/**
	 * ISIP extension
	 */
	protected static final String EXT_ISIP = "ISIP";

	/**
	 * ISIP extension
	 */
	protected static final String EXT_SAFE = "SAFE";

	/**
	 * OBS service
	 */
	private final ObsClient obsClient;

	/**
	 * Output producer factory for message queue system
	 */
	private final OutputProcuderFactory procuderFactory;

	/**
	 * Working directory
	 */
	private final String workDirectory;

	/**
	 * Name of the file where the outputs are listed from the working directory
	 */
	private final String listFile;

	/**
	 * Input message
	 */
	private final GenericMessageDto<IpfExecutionJob> inputMessage;

	/**
	 * List of authorized and family correspondance define in the job
	 */
	private final List<LevelJobOutputDto> authorizedOutputs;

	/**
	 * Size of the batch for upload in OBS
	 */
	private final int sizeUploadBatch;

	/**
	 * Prefix before each monitor logs
	 */
	private final String prefixMonitorLogs;

	/**
	 * Application level
	 */
	private final ApplicationLevel appLevel;

	/**
	 * Application properties
	 */
	private final ApplicationProperties properties;

	public static enum AcquisitionMode {
		EW, IW, SM, WV
	}

	/**
	 * Constructor
	 * 
	 * @param obsClient
	 * @param outputProcuderFactory
	 * @param workDirectory
	 * @param authorizedOutputs
	 * @param listFile
	 * @param sizeS3UploadBatch
	 * @param prefixMonitorLogs
	 * @param properties
	 */
	public OutputProcessor(final ObsClient obsClient, final OutputProcuderFactory procuderFactory,
			final GenericMessageDto<IpfExecutionJob> inputMessage, final String listFile, final int sizeUploadBatch,
			final String prefixMonitorLogs, final ApplicationLevel appLevel, final ApplicationProperties properties) {
		this.obsClient = obsClient;
		this.procuderFactory = procuderFactory;
		this.listFile = listFile;
		this.inputMessage = inputMessage;
		this.authorizedOutputs = inputMessage.getBody().getOutputs();
		this.workDirectory = inputMessage.getBody().getWorkDirectory();
		this.sizeUploadBatch = sizeUploadBatch;
		this.prefixMonitorLogs = prefixMonitorLogs;
		this.appLevel = appLevel;
		this.properties = properties;
	}

	/**
	 * Extract the list of outputs from a file
	 * 
	 * @return
	 * @throws InternalErrorException
	 */
	private List<String> extractFiles() throws InternalErrorException {
		LOGGER.info("{} 1 - Extracting list of outputs", prefixMonitorLogs);
		try {
			return Files.lines(Paths.get(listFile)).collect(Collectors.toList());
		} catch (final IOException ioe) {
			throw new InternalErrorException("Cannot parse result list file " + listFile + ": " + ioe.getMessage(),
					ioe);
		}
	}

	private final ProductFamily familyOf(final LevelJobOutputDto output, final String name) {
		final ProductFamily family = ProductFamily.fromValue(output.getFamily());
		if (family == ProductFamily.L0_SLICE && 
			appLevel == ApplicationLevel.L0 && 
			!name.matches(properties.getSegmentBlacklistPattern())
		) {			
			return ProductFamily.L0_SEGMENT;
		}
		return family;
	}
	
	/**
	 * Sort outputs and convert them into object for message queue system or OBS
	 * according the output define in the job they match
	 * 
	 * @param lines
	 * @param uploadBatch
	 * @param outputToPublish
	 * @param reportToPublish
	 * @throws UnknownFamilyException
	 */
	final long sortOutputs(final List<String> lines, final List<FileObsUploadObject> uploadBatch,
			final List<ObsQueueMessage> outputToPublish, final List<FileQueueMessage> reportToPublish, final ReportingFactory reportingFactory)
			throws AbstractCodedException {

		long productSize = 0;
		
		final OQCExecutor executor = new OQCExecutor(properties);

		LOGGER.info("{} 2 - Starting organizing outputs", prefixMonitorLogs);

		for (final String line : lines) {
			LOGGER.debug("Processing line of list file: {}", line);

			// Extract the product name, the complete filepath, job output and
			// the mode
			final String productName = getProductName(line);
			final String filePath = getFilePath(line, productName);
			final LevelJobOutputDto matchOutput = getMatchOutput(productName);

			// If match process output
			if (matchOutput == null) {
				LOGGER.warn("Output {} ignored because no found matching regular expression", productName);
			} else {
				final ProductFamily family = familyOf(matchOutput, productName);

				final File file = new File(filePath);
				final OQCFlag oqcFlag = executor.executeOQC(file, family, matchOutput, new OQCDefaultTaskFactory(), reportingFactory);
				LOGGER.info("Result of OQC validation was: {}", oqcFlag);

				switch (family) {
				case L0_REPORT:
				case L1_REPORT:
				case L2_REPORT:
				case L0_SEGMENT_REPORT:
					// If report, put in a cache to send report
					LOGGER.info("Output {} (SEGMENT_REPORT) is considered as belonging to the family {}", productName,
							matchOutput.getFamily());
					reportToPublish.add(new FileQueueMessage(family, productName, file));
					productSize += size(file);
					break;
				case L0_SLICE:
				case L0_SEGMENT:	
					// Specific case of the L0 wrapper
					if (appLevel == ApplicationLevel.L0) {						
						final Reporting reporting = reportingFactory.newReporting("GhostHandling");						
						reporting.begin(
								ReportingUtils.newFilenameReportingInputFor(family, productName),
								new ReportingMessage("Checking if %s is a ghost candidate", productName)
						);
						
						final boolean ghostCandidate = isGhostCandidate(productName);

						LOGGER.info("Output {} is recognized as belonging to the family {}", productName, family);
						
						if (!ghostCandidate) {
							LOGGER.info("Product {} is not a ghost candidate and processMode is {}", productName,inputMessage.getBody().getProductProcessMode());							
							reporting.end(
									new GhostHandlingSegmentReportingOutput(false),
									new ReportingMessage("%s (%s) is not a ghost candidate", productName, family)
							);
							uploadBatch.add(new FileObsUploadObject(family, productName, file));
							outputToPublish.add(
								new ObsQueueMessage(family, productName, productName, inputMessage.getBody().getProductProcessMode(),oqcFlag));

						} 
						else {
							LOGGER.info("Product {} is a ghost candidate", productName);
							reporting.end(
									new GhostHandlingSegmentReportingOutput(true),
									new ReportingMessage("%s (%s) is a ghost candidate", productName, family)
							);
							uploadBatch.add(new FileObsUploadObject(ProductFamily.GHOST, productName, file));
						}
						productSize += size(file);
					}
					else {
						LOGGER.info("Output {} is considered as belonging to the family {}", productName,
								matchOutput.getFamily());
						uploadBatch.add(new FileObsUploadObject(family, productName, file));
						outputToPublish.add(new ObsQueueMessage(family, productName, productName,
								inputMessage.getBody().getProductProcessMode(),oqcFlag));
						productSize += size(file);
					}
					break;
					//FIXME There shall not be blanks anymore.
				case L0_BLANK:
					LOGGER.error("Product {} is considered blank", productName);
					//TODO report ERROR
					//ReportingUtils.newReportingBuilder().newEventReporting(new ReportingMessage("Product %s is considered as blank", productName, matchOutput.getFamily()));
					break;
				case L0_ACN:
					// Specific case of the L0 wrapper
					if (appLevel == ApplicationLevel.L0) {
						LOGGER.warn("Product {} is not expected as output of AIO", productName);
						uploadBatch.add(new FileObsUploadObject(family, productName, file));
						outputToPublish.add(new ObsQueueMessage(family, productName, productName,
								inputMessage.getBody().getProductProcessMode(),oqcFlag));
						productSize += size(file);
					} else {
						LOGGER.info("Output {} (ACN, BLANK) is considered as belonging to the family {}", productName,
								matchOutput.getFamily());
						uploadBatch.add(new FileObsUploadObject(family, productName, file));
						outputToPublish.add(new ObsQueueMessage(family, productName, productName,
								inputMessage.getBody().getProductProcessMode(),oqcFlag));
						productSize += size(file);
					}
					break;
				case L1_SLICE:
				case L1_ACN:
				case L2_SLICE:
				case L2_ACN:
					// If compatible object storage, put in a cache to
					// upload per batch
					LOGGER.info("Output {} is considered as belonging to the family {}", productName,
							matchOutput.getFamily());
					uploadBatch.add(new FileObsUploadObject(family, productName, file));
					outputToPublish.add(new ObsQueueMessage(family, productName, productName,
							inputMessage.getBody().getProductProcessMode(), oqcFlag));
					productSize += size(file);
					break;
				case BLANK:
					LOGGER.info("Output {} will be ignored", productName);
					break;
				default:
					throw new UnknownFamilyException("Family not managed in output processor ", family.name());
				}
			}

		}
		return productSize;
	}
	
	

	/**
	 * This method takes the product name and returns if this product is a possible
	 * candidate for a ghost product
	 * 
	 * @param productName The product name of the product that should be checked
	 * @return Either true or false depending if the product was identified as ghost
	 *         product. If an error occurs during the extraction, the product will
	 *         be identified as non-ghost
	 */
	boolean isGhostCandidate(final String productName) {
		LOGGER.info("Performing ghost candidate check for product '{}'", productName);

		// Something completely unexpected was provided as product Name
		if (productName.length() < 48) {
			LOGGER.info("Product length was less than 48 characters");
			return false;
		}

		/*
		 * A typical product name would be:
		 * S1A_ES_RAW__0SVV_20190810T225025_20190810T225412_028513_033938_569F We are
		 * extracting the sensing start and stop time from the product name and try to
		 * convert them into a LocalDateTime. If something goes wrong, we are handling
		 * here something else than a ghost candidate
		 */
		final String startDateString = productName.substring(17, 32);
		final String endDateString = productName.substring(33, 48);

		Duration duration = null;
		try {

			final LocalDateTime startTime = LocalDateTime.parse(startDateString,
					DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
			final LocalDateTime endTime = LocalDateTime.parse(endDateString,
					DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
			LOGGER.trace("Extracted dates from ghost candidate: startDate {}, endDate {}, polarisation {}", startTime,
					endTime);

			duration = Duration.between(startTime, endTime);
		} catch (final DateTimeParseException ex) {
			LOGGER.error("Sensing time from product Name is not valid: startTime={}, endTime={}. This is not a ghost candidate.", startDateString,
					endDateString);
			return false;
		}
		
		// Now we are also trying to extract the acquisition mode from the product.
		AcquisitionMode acquisitionMode = null;		
		try {
			String acquisitionModeStr = productName.substring(4, 6);
			
			// This is a special case for ACN being actual SM
			if (acquisitionModeStr.matches("S[0-6]")) {
				acquisitionModeStr = "SM";
			}
			acquisitionMode = AcquisitionMode.valueOf(acquisitionModeStr);
			LOGGER.trace("Extracted acquisition mode from ghost candidate: acquisitionMode={}", acquisitionMode);
		} catch (final IllegalArgumentException ex) {
			LOGGER.error("Acquisition mode from product name is not valid: acquisitionMode={}. This is not a ghost candidate.", acquisitionMode);
			return false;
		}

		LOGGER.info("Information used for ghost candidate detection: duration {}, acquisitionMode {}",
				duration.getSeconds(), acquisitionMode);

		// if the configured length is smaller than the duration, its a candidate
		final long ghostLength = ghostLength(acquisitionMode);
		if (duration.getSeconds() <= ghostLength) {
			LOGGER.info("Ghost length is {}, but duration is {}. This is a ghost candidate!", ghostLength,
					duration.getSeconds());
			return true;
		}

		// No indication so far that this is a ghost candidate
		LOGGER.info("Product length is valid. This is not a ghost candidate!");
		return false;
	}

	/**
	 * This method is using the polarisation to lookup the configured product length
	 * when it is decided that the product might be a possible ghost candidate.
	 * 
	 * @param polarisation The polarisaton that shall be looked up
	 * @return Length in seconds when it is decided to be a ghost candidate
	 */
	private long ghostLength(final AcquisitionMode polarisation) {
		switch (polarisation) {
		case EW:
			return properties.getThresholdEw();
		case IW:
			return properties.getThresholdIw();
		case SM:
			return properties.getThresholdSm();
		case WV:
			return properties.getThresholdWv();
		}

		// This should not happen other than enum was modified
		throw new IllegalArgumentException("Unknown polarisation provided");
	}

	/**
	 * Extract the product name from the line of the result file
	 * 
	 * @param line
	 * @return
	 */
	private String getProductName(final String line) {
		// Extract the product name and the complete filepath
		// First, remove the first directory (NRT or REPORT)
		String productName = line;
		final int index = line.indexOf('/');
		if (index != -1) {
			productName = line.substring(index + 1);
		}
		// Second: if file ISIP, retrieve only .SAFE
		if (productName.toUpperCase().endsWith(EXT_ISIP)) {
			productName = productName.substring(0, productName.length() - EXT_ISIP.length()) + EXT_SAFE;
		}
		return productName;
	}

	/**
	 * Build the path for the object to upload. In most of the cases, it is the
	 * concatenation of the working directory and the line. But for .ISIP files, we
	 * considers the .SAFE file is the .ISIP directory
	 * 
	 * @param line
	 * @param productName
	 * @return
	 */
	private String getFilePath(final String line, final String productName) {
		String filePath = workDirectory + line;
		// Second: if file ISIP, retrieve only .SAFE
		if (line.toUpperCase().endsWith("ISIP")) {
			filePath = filePath + File.separator + productName;
		}
		return filePath;
	}

	/**
	 * Search if a output defined in the job matches with the product name
	 * 
	 * @param productName
	 * @return
	 */
	private LevelJobOutputDto getMatchOutput(final String productName) {
		for (final LevelJobOutputDto jobOutputDto : authorizedOutputs) {
			if (Pattern.matches(jobOutputDto.getRegexp().substring(workDirectory.length()), productName)) {
				return jobOutputDto;
			}
		}
		return null;
	}

	/**
	 * Process product: upload in OBS and publish in message queue system per batch
	 * 
	 * @param uploadBatch
	 * @param outputToPublish
	 * @throws AbstractCodedException
	 * @throws ObsEmptyFileException 
	 */
	final void processProducts(
			final ReportingFactory reportingFactory, 
			final List<FileObsUploadObject> uploadBatch,
			final List<ObsQueueMessage> outputToPublish,
			final UUID uuid
	) throws AbstractCodedException, ObsEmptyFileException {

		final double size = Double.valueOf(uploadBatch.size());
		final double nbPool = Math.ceil(size / sizeUploadBatch);

		for (int i = 0; i < nbPool; i++) {
			final int lastIndex = Math.min((i + 1) * sizeUploadBatch, uploadBatch.size());
			final List<FileObsUploadObject> sublist = uploadBatch.subList(i * sizeUploadBatch, lastIndex);

			if (i > 0) {
				this.publishAccordingUploadFiles(i - 1, sublist.get(0).getKey(), outputToPublish, uuid);
			}
			try {

				if (Thread.currentThread().isInterrupted()) {
					throw new InternalErrorException("The current thread as been interrupted");
				}
				this.obsClient.upload(sublist, reportingFactory);
			} catch (final AbstractCodedException | ObsEmptyFileException e) {
				throw e;
			}
		}
		publishAccordingUploadFiles(nbPool - 1, NOT_KEY_OBS, outputToPublish, uuid);
	}

	/**
	 * Public uploaded files, i.e. unitl the output to publish is the next key to
	 * upload
	 * 
	 * @param nbBatch
	 * @param nextKeyUpload
	 * @param outputToPublish
	 * @throws AbstractCodedException
	 */
	private void publishAccordingUploadFiles(
			final double nbBatch,
			final String nextKeyUpload, 
			final List<ObsQueueMessage> outputToPublish,
			final UUID uuid
	) throws AbstractCodedException {

		LOGGER.info("{} 3 - Publishing KAFKA messages for batch {}", prefixMonitorLogs, nbBatch);
		final Iterator<ObsQueueMessage> iter = outputToPublish.iterator();
		boolean stop = false;
		while (!stop && iter.hasNext()) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InternalErrorException("The current thread as been interrupted");
			}
			final ObsQueueMessage msg = iter.next();
			if (nextKeyUpload.startsWith(msg.getKeyObs())) {
				stop = true;
			} else {
				try {
					procuderFactory.sendOutput(msg, inputMessage, uuid);
				} catch (final MqiPublicationError ace) {
				}
				iter.remove();
			}

		}
	}

	/**
	 * Publish reports in message queue system
	 * 
	 * @param reportToPublish
	 * @throws AbstractCodedException
	 */
	protected void processReports(final List<FileQueueMessage> reportToPublish,	final UUID uuid) throws AbstractCodedException {

		LOGGER.info("{} 4 - Starting processing not object storage compatible outputs", prefixMonitorLogs);
		if (!reportToPublish.isEmpty()) {
			for (final FileQueueMessage msg : reportToPublish) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InternalErrorException("The current thread as been interrupted");
				} else {
					LOGGER.info("{} 4 - Publishing KAFKA message for output {}", prefixMonitorLogs,
							msg.getProductName());
					try {
						procuderFactory.sendOutput(msg, inputMessage, uuid);
					} catch (final MqiPublicationError ace) {
						final String message = String.format("%s [code %d] %s", prefixMonitorLogs, ace.getCode().getCode(),
								ace.getLogMessage());
						LOGGER.error(message);
					}
				}
			}
		}
	}

	/**
	 * Function which process all the output of L0 process
	 * @throws ObsEmptyFileException 
	 * 
	 * @throws ObsException
	 * @throws IOException
	 * @throws ObsEmptyFileException 
	 */
	public ReportingOutput processOutput(final ReportingFactory reportingFactory, final UUID uuid) throws AbstractCodedException, ObsEmptyFileException {
		final List<String> filenames = new ArrayList<>();
		final List<String> segments = new ArrayList<>();
		// Extract files
		final List<String> lines = extractFiles();

		// Sort outputs
		final List<FileObsUploadObject> uploadBatch = new ArrayList<>();
		final List<ObsQueueMessage> outputToPublish = new ArrayList<>();
		final List<FileQueueMessage> reportToPublish = new ArrayList<>();
			
		sortOutputs(lines, uploadBatch, outputToPublish, reportToPublish, reportingFactory);
		try {
			// Upload per batch the output
			processProducts(reportingFactory, uploadBatch, outputToPublish, uuid);
			// Publish reports
			processReports(reportToPublish, uuid);
			for (final FileObsUploadObject obj : uploadBatch) {
				if (obj.getFamily() == ProductFamily.L0_SEGMENT) {
					segments.add(obj.getKey());
				}
				else {
					filenames.add(obj.getKey());
				}
			}
		} catch (final AbstractCodedException | ObsEmptyFileException e) {
			throw e;
		}
		return new FilenameReportingOutput(filenames, segments);
	}

	private long size(final File file) throws InternalErrorException {
		try {
			final Path folder = file.toPath();
			return Files.walk(folder).filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();

		} catch (final IOException e) {
			// TODO to have the tests running without actual files
			return 0L;
		}
	}
}
