package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.BrowseImage;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.ObsQueueMessage;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc.OQCDefaultTaskFactory;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc.OQCExecutor;
import esa.s1pdgs.cpoc.ipf.execution.worker.service.report.GhostHandlingSegmentReportingOutput;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

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
	 * OBS service
	 */
	private final ObsClient obsClient;

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
	private final IpfExecutionJob inputMessage;

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
	
	private final boolean debugMode;

	public enum AcquisitionMode {
		EW, IW, SM, WV, RF
	}
	
	private final OutputUtils outputUtils;

	/**
	 * FIXME replace legacy constructor
	 * Legacy Constructor
	 * 
	 */
	@Deprecated
	public OutputProcessor(
			final ObsClient obsClient, 
			final IpfExecutionJob inputMessage, 
			final String listFile, 
			final int sizeUploadBatch,
			final String prefixMonitorLogs, 
			final ApplicationLevel appLevel, 
			final ApplicationProperties properties
	) {
		this(
				obsClient,
				inputMessage.getWorkDirectory(),
				listFile,
				inputMessage,
				inputMessage.getOutputs(),
				sizeUploadBatch,
				prefixMonitorLogs,
				appLevel,
				properties,
				inputMessage.isDebug());
	}
	
	public OutputProcessor(
			final ObsClient obsClient,
			final String workDirectory,
			final String listFile,
			final IpfExecutionJob inputMessage,
			final List<LevelJobOutputDto> authorizedOutputs,
			final int sizeUploadBatch,
			final String prefixMonitorLogs,
			final ApplicationLevel appLevel,
			final ApplicationProperties properties,
			final boolean debugMode) {
		this.obsClient = obsClient;
		this.workDirectory = workDirectory;
		this.listFile = listFile;
		this.inputMessage = inputMessage;
		this.authorizedOutputs = authorizedOutputs;
		this.sizeUploadBatch = sizeUploadBatch;
		this.prefixMonitorLogs = prefixMonitorLogs;
		this.appLevel = appLevel;
		this.properties = properties;
		this.debugMode = debugMode;
		
		this.outputUtils = new OutputUtils(properties, prefixMonitorLogs);
	}
	
	/**
	 * Sort outputs and convert them into object for message queue system or OBS
	 * according the output define in the job they match
	 * 
	 */
	final void sortOutputs(final List<String> lines, final List<FileObsUploadObject> uploadBatch,
			final List<ObsQueueMessage> outputToPublish, final List<FileQueueMessage> reportToPublish, final ReportingFactory reportingFactory)
			throws AbstractCodedException {

		final OQCExecutor executor = new OQCExecutor(properties);

		LOGGER.info("{} 2 - Starting organizing outputs", prefixMonitorLogs);

		for (final String line : lines) {
			LOGGER.debug("Processing line of list file: {}", line);

			// Extract the product name, the complete filepath, job output and
			// the mode
			final String productName = outputUtils.getProductName(line);
			final String filePath = getFilePath(line, productName);
			final LevelJobOutputDto matchOutput = getMatchOutput(productName);

			// If match process output
			if (matchOutput == null) {
				LOGGER.warn("Output {} ignored because no found matching regular expression", productName);
			} else {
				final ProductFamily family = outputUtils.familyOf(matchOutput, appLevel);

				final File file = new File(filePath);
				final OQCFlag oqcFlag = executor.executeOQC(file, family, matchOutput, new OQCDefaultTaskFactory(), reportingFactory);
				final long productSizeBytes = size(file);
				
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
						
						final boolean ghostCandidate = isGhostCandidate(file);

						LOGGER.info("Output {} is recognized as belonging to the family {}", productName, family);
						
						if (!ghostCandidate) {
							LOGGER.info("Product {} is not a ghost candidate and processMode is {}", productName,inputMessage.getProductProcessMode());							
							reporting.end(
									new GhostHandlingSegmentReportingOutput(false),
									new ReportingMessage("%s (%s) is not a ghost candidate", productName, family)
							);
							addToUploadAndPublishIfNotExistsInObs(uploadBatch, outputToPublish, productName, family,
									file, oqcFlag, productSizeBytes);

						} 
						else {
							LOGGER.info("Product {} is a ghost candidate", productName);
							reporting.end(
									new GhostHandlingSegmentReportingOutput(true),
									new ReportingMessage("%s (%s) is a ghost candidate", productName, family)
							);
							uploadBatch.add(newUploadObject(ProductFamily.GHOST,productName, file));
						}
					}
					else {
						LOGGER.info("Output {} is considered as belonging to the family {}", productName,
								matchOutput.getFamily());						
						addToUploadAndPublishIfNotExistsInObs(uploadBatch, outputToPublish, productName, family, file,
								oqcFlag, productSizeBytes);
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
						addToUploadAndPublishIfNotExistsInObs(uploadBatch, outputToPublish, productName, family, file,
								oqcFlag, productSizeBytes);
					} else {
						LOGGER.info("Output {} (ACN, BLANK) is considered as belonging to the family {}", productName,
								matchOutput.getFamily());
						addToUploadAndPublishIfNotExistsInObs(uploadBatch, outputToPublish, productName, family, file,
								oqcFlag, productSizeBytes);
					}
					break;
				case L1_SLICE:
				case L1_ACN:
				case L2_SLICE:
				case L2_ACN:
				case SPP_MBU: //just trying
				case SPP_OBS: //just trying
				case S3_GRANULES:
				case S3_AUX:
				case S3_CAL:
				case S3_L0:
				case S3_L1_NRT:
				case S3_L1_STC:
				case S3_L1_NTC:
				case S3_L2_NRT:
				case S3_L2_STC:
				case S3_L2_NTC:
				case S3_PUG:
					// If compatible object storage, put in a cache to
					// upload per batch
					LOGGER.info("Output {} is considered as belonging to the family {}", productName,
							matchOutput.getFamily());
					addToUploadAndPublishIfNotExistsInObs(uploadBatch, outputToPublish, productName, family, file,
							oqcFlag, productSizeBytes);
					break;
				case BLANK:
					LOGGER.info("Output {} will be ignored", productName);
					break;
				default:
					throw new UnknownFamilyException("Family not managed in output processor ", family.name());
				}
			}

		}
	}

	private void addToUploadAndPublishIfNotExistsInObs(final List<FileObsUploadObject> uploadBatch,
			final List<ObsQueueMessage> outputToPublish, final String productName, final ProductFamily family,
			final File file, final OQCFlag oqcFlag, final long productSizeBytes) throws InternalErrorException{
		
		try {
			if (obsClient.existsWithSameSize(new ObsObject(family, productName), productSizeBytes)) {
				
				LOGGER.warn("Output {} with family {} already exists in OBS with same size of {} bytes and upload will be skipped",
						productName, family, productSizeBytes );
				
			} else {
				uploadBatch.add(newUploadObject(family, productName, file));
				outputToPublish.add(new ObsQueueMessage(family, productName, productName,
						inputMessage.getProductProcessMode(), oqcFlag, productSizeBytes));
				
			}
		} catch (ObsException | SdkClientException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	static boolean isPartial(final File file) {		
		final File manifest = new File(file,"manifest.safe");
		try {
			return manifest.exists() && FileUtils.readFile(manifest)
					.contains("<productConsolidation>PARTIAL</productConsolidation>");
		} catch (final InternalErrorException e) {
			LOGGER.error(Exceptions.messageOf(e), e);
			return false;
		}
	}
	
	

	/**
	 * This method takes the product name and returns if this product is a possible
	 * candidate for a ghost product
	 * 
	 * @param file The product file that should be checked
	 * @return Either true or false depending if the product was identified as ghost
	 *         product. If an error occurs during the extraction, the product will
	 *         be identified as non-ghost
	 */
	boolean isGhostCandidate(final File file) {
		final String productName = file.getName();
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

		Duration duration;
		try {

			final LocalDateTime startTime = LocalDateTime.parse(startDateString,
					DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
			final LocalDateTime endTime = LocalDateTime.parse(endDateString,
					DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
			LOGGER.trace("Extracted dates from ghost candidate: startDate {}, endDate {}", startTime,
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
		
		// S1PRO-2420: PARTIAL products of RF products shall be treated as ghosts
		if (acquisitionMode == AcquisitionMode.RF) {	
			if (isPartial(file)){
				LOGGER.info("Partial RF {} marked as ghost", productName);
				return true;
			}
			else {
				LOGGER.info("RF {} is not ghost", productName);
				return false;
			}
		}

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
	 * Build the path for the object to upload. In most of the cases, it is the
	 * concatenation of the working directory and the line. But for .ISIP files, we
	 * considers the .SAFE file is the .ISIP directory
	 * 
	 */
	private String getFilePath(final String line, final String productName) {
		String filePath = workDirectory + line;
		// Second: if file ISIP, retrieve only .SAFE
		if (properties.isChangeIsipToSafe() && line.toUpperCase().endsWith("ISIP")) {
			filePath = filePath + File.separator + productName;
		}
		return filePath;
	}

	/**
	 * Search if a output defined in the job matches with the product name
	 * 
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
	 */
	final List<Message<CatalogJob>> processProducts(
			final ReportingFactory reportingFactory, 
			final List<FileObsUploadObject> uploadBatch,
			final List<ObsQueueMessage> outputToPublish,
			final UUID uuid,
			final String t0PdgsDate
	) throws Exception {
		// I can't believe this stuff is actually working in any reliable form. It seems to be operating on
		// 2 indepenent lists associated via the obsKey. This REALLY needs some refactoring since there are
		// some dangerous assumptions here, like:
		// - the two lists need to be in any coherence to each other that seems to be the case at the moment
		// but I'm quite surprised it's even working. If the lists content are changed any time in the future
		// this might break horribly.
		// - publishing of the outputs is done before the upload, i.e. all subsequent steps must have a 
		// reliable retry mechanism. If OBS upload fails, there may be Zombie messages in the system referring
		// to products that have not been uploaded.
		// As time is scarce, all this crap needs to be cleaned up in the IPF refactoring story in the future
		// and this piece of code should never become operational.
		final List<Message<CatalogJob>> res = new ArrayList<>();
		
		final double size = uploadBatch.size();
		final int nbPool = (int) Math.ceil(size / sizeUploadBatch);

		for (int i = 0; i < nbPool; i++) {
			final int lastIndex = Math.min((i + 1) * sizeUploadBatch, uploadBatch.size());
			final List<FileObsUploadObject> sublist = uploadBatch.subList(i * sizeUploadBatch, lastIndex);

			if (i > 0) {
				res.addAll(publishAccordingUploadFiles(i - 1, sublist.get(0).getKey(), outputToPublish, uuid, t0PdgsDate));
			}
			if (Thread.currentThread().isInterrupted()) {
				throw new InternalErrorException("The current thread as been interrupted");
			}
			obsClient.upload(sublist, reportingFactory);
		}
		// ok, this seems to be some kind of 'poison pill' pattern here to indicate that upload is done.
		// as nothing else is done. If there is a remainder in 'outputToPublish', I guess it will be published
		// but it will not be uploaded. But to be safe, we add it also here...
		res.addAll(publishAccordingUploadFiles(nbPool - 1, NOT_KEY_OBS, outputToPublish, uuid, t0PdgsDate));
		return res;
	}

	private FileObsUploadObject newUploadObject(final ProductFamily family, final String productName, final File file) {
		return new FileObsUploadObject(
				family, 
				productName, 
				file
		);
	}
	
	/**
	 * Public uploaded files, i.e. unitl the output to publish is the next key to
	 * upload
	 * 
	 */
	private List<Message<CatalogJob>> publishAccordingUploadFiles(
			final int nbBatch,
			final String nextKeyUpload, 
			final List<ObsQueueMessage> outputToPublish,
			final UUID uuid,
			final String t0PdgsDate
	) throws Exception {
		final List<Message<CatalogJob>> result = new ArrayList<>();
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
				result.add(MessageBuilder.withPayload(publish(uuid, msg, t0PdgsDate)).build());
				iter.remove();
			}

		}
		return result;
	}

	private CatalogJob publish(
			final UUID uuid, 
			final ObsQueueMessage msg,
			final String t0PdgsDate
	) throws Exception {
		try {
			LOGGER.info("{} 3 - Publishing KAFKA message for output {}", prefixMonitorLogs,
					msg.getProductName());
			final CatalogJob res = new CatalogJob(msg.getProductName(), msg.getKeyObs(), msg.getFamily(),
					toUppercaseOrNull(msg.getProcessMode()), msg.getOqcFlag(), inputMessage.getTimeliness(), uuid);
			res.setMissionId(inputMessage.getMissionId());
			res.getAdditionalFields().put("t0PdgsDate", t0PdgsDate);
			res.setProductSizeByte(msg.getProductSizeBytes());
			LOGGER.info("{} 3 - Successful published KAFKA message for output {}", prefixMonitorLogs,
					msg.getProductName());
			return res;
		} catch (final Exception e) {
			LOGGER.error("{} 3 - Failed publishing KAFKA message for output {}", prefixMonitorLogs,
					msg.getProductName());
			throw e;
		}
	}
	
	/**
	 * Utility method for conversion of product mode into Uppercase
	 */
	private final String toUppercaseOrNull(final String string)
    {
    	if (string == null)
    	{
    		return null;
    	}
    	return string.toUpperCase();
    }

	/**
	 * Publish reports in message queue system
	 * 
	 */
	protected void processReports(final List<FileQueueMessage> reportToPublish,	final UUID uuid) throws AbstractCodedException {

		LOGGER.info("{} 4 - Starting processing not object storage compatible outputs", prefixMonitorLogs);
		if (!reportToPublish.isEmpty()) {
			for (final FileQueueMessage msg : reportToPublish) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InternalErrorException("The current thread as been interrupted");
				} else {
					try {
						LOGGER.info("{} 4 - Publishing KAFKA message for output {}", prefixMonitorLogs,
								msg.getProductName());
						// TODO: fix this!
						//procuderFactory.sendOutput(msg, inputMessage, uuid);
						LOGGER.info("{} 4 - Successful published KAFKA message for output {}", prefixMonitorLogs,
								msg.getProductName());
					} catch (final Exception e) {
						LOGGER.error("{} 4 - Failed publishing KAFKA message for output {}", prefixMonitorLogs,
								msg.getProductName());
						throw e;
					}
				}
			}
		}
	}

	/**
	 * Function which process all the output of L0 process
	 */
	public List<Message<CatalogJob>> processOutput(
			final ReportingFactory reportingFactory, 
			final UUID uuid,
			final IpfExecutionJob job			  
	) throws Exception {
		// Sort outputs
		final List<FileObsUploadObject> uploadBatch = new ArrayList<>();
		final List<ObsQueueMessage> outputToPublish = new ArrayList<>();
		final List<FileQueueMessage> reportToPublish = new ArrayList<>();
			
		// S1PRO-1856: for debug, no publishing and upload will be into OBS DEBUG bucket
		if (debugMode) {			
			final String debugPrefix = debugOutputPrefix(properties.getHostname(),uuid,job);	

			final FileObsUploadObject upload = newUploadObject(
					ProductFamily.DEBUG, 
					debugPrefix, 
					new File(workDirectory)
			);
			obsClient.upload(Collections.singletonList(upload), reportingFactory);
			
			// always fail, if debug mode is set		
			throw new IllegalStateException(
					String.format(
							"Successfully produced outputs in debugMode and uploaded results to debug bucket at: %s", 
							debugPrefix
					)
			); 			
		}

		// Extract files
		final List<String> lines = outputUtils.extractFiles(listFile, workDirectory);
		
		sortOutputs(lines, uploadBatch, outputToPublish, reportToPublish, reportingFactory);
	

		// Upload per batch the output
		// S1PRO-1494: WARNING--- list will be emptied by this method. For reporting, make a copy beforehand
		//final List<ObsQueueMessage> outs = new ArrayList<>(outputToPublish);
		final List<Message<CatalogJob>> res = processProducts(
						reportingFactory,
						uploadBatch,
						outputToPublish,
						uuid, 
						(String) job.getAdditionalFields().get("t0PdgsDate"));
		processBrowseImages(reportingFactory, uploadBatch);
		// Publish reports
		processReports(reportToPublish, uuid);	
		return res;
	}
	
	private void processBrowseImages(final ReportingFactory reportingFactory, final List<FileObsUploadObject> uploadBatch)
			throws IOException, AbstractCodedException, ObsEmptyFileException {

		if (MissionId.S1.name().equals(inputMessage.getMissionId())) {
			for (FileObsUploadObject o : uploadBatch) {
				if (o.getFile().isDirectory()) {
					Path previewPath = o.getFile().toPath().resolve(BrowseImage.S1_BROWSE_IMAGE_DIRECTORY);
					if (previewPath.toFile().exists()) {
						
						List<Path> pngFiles = new ArrayList<>();
						try (Stream<Path> pathStream =  Files.list(previewPath).filter(a -> a.toString().endsWith(BrowseImage.S1_BROWSE_IMAGE_FORMAT))) {
							pngFiles = pathStream.collect(Collectors.toList());
						}
						if (pngFiles.size() == 1) {
							obsClient.upload(Collections.singletonList(
									newUploadObject(o.getFamily(), BrowseImage.s1BrowseImageName(o.getKey()), pngFiles.get(0).toFile())),
									reportingFactory);
						}
					}
				}
			}
		}
	}
	
	
	final String debugOutputPrefix(	
			final String hostname,
			final UUID uuid,
			final IpfExecutionJob job
	) {
		return hostname + "_" + 
				job.getKeyObjectStorage() + "_" + 
				uuid.toString() + "_" + 
				job.getRetryCounter();
	}

	private long size(final File file) throws InternalErrorException {
		try {
			final Path folder = file.toPath();
			try (Stream<Path> walk = Files.walk(folder)) {
				return walk.filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
			}

		} catch (final IOException e) {
			// TODO to have the tests running without actual files
			return 0L;
		}
	}
}
