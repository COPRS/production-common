package esa.s1pdgs.cpoc.ipf.execution.worker.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.DevProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.MonitorLogUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.WorkingDirectoryUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.InputDownloader;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.OutputEstimation;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.OutputProcessor;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.ipf.execution.worker.service.report.IpfFilenameReportingOutput;
import esa.s1pdgs.cpoc.ipf.execution.worker.service.report.JobReportingInput;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.UnrecoverableErrorAwareObsClient;
import esa.s1pdgs.cpoc.report.MissingOutput;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;

/**
 * Process a jobs
 * <li>Launch in a thread the processes execution which will wait for being
 * active once the minimal inputs are download</li>
 * <li>Create necessary directories and files, download inputs and inform
 * process executor when it can start</li>
 * <li>Wait for processes execution end</li>
 * <li>Process outputs</li>
 * 
 * @author Viveris Technologies
 */
public class ExecutionWorkerService implements Function<IpfExecutionJob, List<Message<CatalogJob>>> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(ExecutionWorkerService.class);

	private final CommonConfigurationProperties commonProperties;
	
	/**
	 * AppStatus
	 */
	private final AppStatus appStatus;

	/**
	 * Development properties
	 */
	private final DevProperties devProperties;

	/**
	 * Application properties
	 */
	private final ApplicationProperties properties;

	/**
	 * Output processsor
	 */
	private final ObsClient obsClient;
	
	/**
	 */
	@Autowired
	public ExecutionWorkerService(final CommonConfigurationProperties commonProperties, final AppStatus appStatus, final ApplicationProperties properties,
			final DevProperties devProperties, final ObsClient obsClient) {
		this.commonProperties = commonProperties;
		this.appStatus = appStatus;
		this.devProperties = devProperties;
		this.properties = properties;
		this.obsClient = new UnrecoverableErrorAwareObsClient(obsClient, e -> appStatus.getStatus().setFatalError());
	}

	@Override
	public List<Message<CatalogJob>> apply(IpfExecutionJob job) {
		LOGGER.info("Initializing job processing for ExecutionJob {}", job.getUid().toString());

		// ----------------------------------------------------------
		// Initialize processing
		// ------------------------------------------------------
		MissionId mission = MissionId.valueOf((String) job.getPreparationJob().getCatalogEvent()
				.getMetadata().get(MissionId.FIELD_NAME));

		final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(job.getUid())
				.newReporting("JobProcessing");		
		
		/*
		 * If the working directory provided by the job order is outside the expected
		 * and configured working directory of the wrapper, something is going on
		 * terribly wrong. Either the working directory configured is on the wrong
		 * location or the job order generation was providing some unexpected result.
		 * Either way, we reject the request.
		 */
		if (!job.getWorkDirectory().startsWith(properties.getWorkingDir())) {
			final String errorMessage = String.format(
					"Attempt to access directory '%s' being outside of working directory '%s'.", job.getWorkDirectory(),
					properties.getWorkingDir());
			
			throw new RuntimeException(errorMessage);
		}

		// Everything is fine with the request, we can start processing it.
		LOGGER.debug("Everything is fine with the request, start processing job {}", job);
		
		final File workdir = new File(job.getWorkDirectory());
		final String jobOrderName = new File(job.getJobOrder()).getName();
		
		final ProductCategory category;

		// Build output list filename
		// TODO the file name of the output.LIST file should be configurable
		final String outputListFile;
		if (properties.getLevel() == ApplicationLevel.L0) {
			outputListFile = job.getWorkDirectory() + "AIOProc.LIST";
			category = ProductCategory.LEVEL_SEGMENTS;
		} else if (properties.getLevel() == ApplicationLevel.L0_SEGMENT) {
			outputListFile = job.getWorkDirectory() + "L0ASProcList.LIST";
			category = ProductCategory.LEVEL_PRODUCTS;
		} else if (EnumSet
				.of(ApplicationLevel.S3_L0, ApplicationLevel.S3_L1, ApplicationLevel.S3_L2, ApplicationLevel.S3_PDU)
				.contains(properties.getLevel())) {
			outputListFile = "*.LIST";
			category = ProductCategory.S3_PRODUCTS;
		} else if(properties.getLevel() == ApplicationLevel.SPP_MBU) {
			outputListFile = job.getWorkDirectory() + workdir.getName() + ".LIST";
			category = ProductCategory.SPP_MBU_PRODUCTS;
		} else if(properties.getLevel() == ApplicationLevel.SPP_OBS) {
			outputListFile = job.getWorkDirectory() + workdir.getName() + ".LIST";
			category = ProductCategory.SPP_PRODUCTS;
		}
		else {
			outputListFile = job.getWorkDirectory() + workdir.getName() + ".LIST";
			category = ProductCategory.LEVEL_PRODUCTS;
		}
		
		// Clean up the working directory with all of its content
		eraseWorkingDirectory(properties.getWorkingDir());

		LOGGER.debug("Output list build {}", outputListFile);

		final PoolExecutorCallable procExecutor = new PoolExecutorCallable(
				properties, 
				job,
				getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS, job), 
				properties.getLevel(), 
				reporting,
				properties.getPlaintextTaskPatterns()
		);
		
		
		final ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);
		final InputDownloader inputDownloader = new InputDownloader(
				obsClient, 
				job.getWorkDirectory(), 
				job.getInputs(),
				properties.getSizeBatchDownload(), 
				getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job),
				procExecutor, 
				properties.getLevel(),
				properties.getPathJobOrderXslt()
		);

//		this.authorizedOutputs = inputMessage.getBody().getOutputs();
//		this.workDirectory = inputMessage.getBody().getWorkDirectory();
//		this.debugMode = inputMessage.getDto().isDebug();
		
		final OutputProcessor outputProcessor = new OutputProcessor(
				obsClient,
				job.getWorkDirectory(),
				outputListFile,
				job, 
				job.getOutputs(),
				properties.getSizeBatchUpload(), 
				getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job),
				properties.getLevel(), 
				properties,
				job.isDebug());
		
		reporting.begin(
				JobReportingInput.newInstance(toReportFilenames(job), jobOrderName, extractIpfVersionFromJobOrder(job)),	
				new ReportingMessage("Start job processing")
		);
		
		List<Message<CatalogJob>> result = new ArrayList<>();
		List<MissingOutput> missingOutputs = new ArrayList<>();
		OutputEstimation outputEstimation = new OutputEstimation(
				properties, 
				job,
				getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job), 
				outputListFile, 
				missingOutputs);
		try {
			result = processJob(job, inputDownloader, outputProcessor, outputEstimation, procExecutorSrv, procCompletionSrv, procExecutor, reporting);
		} catch (Exception e) {
			reporting.error(errorReportMessage(e), missingOutputs);
			throw new RuntimeException(e);
		}
		
		reporting.end(toReportingOutput(result, job.isDebug(), job.getT0_pdgs_date()), new ReportingMessage("End job processing"), missingOutputs);
		return result;
	}

	protected List<Message<CatalogJob>> processJob(final IpfExecutionJob job,
			final InputDownloader inputDownloader,
			final OutputProcessor outputProcessor,
			final OutputEstimation outputEstimation,
			final ExecutorService procExecutorSrv, final ExecutorCompletionService<Void> procCompletionSrv,
			final PoolExecutorCallable procExecutor,
			final Reporting reporting) /* TODO: Refactor to not expect an already begun reporting... */
			throws Exception {
		boolean poolProcessing = false;
		final List<Message<CatalogJob>> catalogJobs = new ArrayList<>();
		
		try {
			LOGGER.info("{} Starting process executor", getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS, job));
			final Future<?> submittedFuture = procCompletionSrv.submit(procExecutor);
			poolProcessing = true;

			if (devProperties.getStepsActivation().get("download")) {
				checkThreadInterrupted();
				LOGGER.info("{} Preparing local working directory",
						getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job));
				inputDownloader.processInputs(reporting);
			} else {
				LOGGER.info("{} Preparing local working directory bypassed",
						getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job));
			}
			waitForPoolProcessesEnding(getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR, job), submittedFuture,
					procCompletionSrv, properties.getTmProcAllTasksS() * 1000L);
			poolProcessing = false;
			
			if (devProperties.getStepsActivation().get("upload")) {
				checkThreadInterrupted();
				LOGGER.info("{} Processing l0 outputs", getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
				catalogJobs.addAll(outputProcessor.processOutput(reporting, reporting.getUid(), job));
			} else {
				LOGGER.info("{} Processing l0 outputs bypasssed", getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
			}
			
			/* 
			 * This code is not used any more in the SCDF context:
			 */
			
			//          // If there was a missing chunk, we submit a warning in order to deal with the
			//          // missing chunk and operator needs to decide, whether to restart it or delete
			//           s// it.

			
			//			final List<String> missingChunks = downloadToBatch.stream()
			//					.filter(o -> o.getFamily() == ProductFamily.INVALID).map(ObsObject::getKey)
			//					.collect(Collectors.toList());
			//
			//			final String warningMessage;
			//			if (!missingChunks.isEmpty()) {
			//				warningMessage = String.format(
			//						"Missing RAWs detected for successful production %s: %s. "
			//								+ "Restart if chunks become available or delete this request if they are lost",
			//						job.getUid().toString(), missingChunks);
			//			} else if (job.isTimedOut()) {
			//				warningMessage = String.format(
			//						"JobGeneration timed out before successful production %s. "
			//								+ "Restart if missing inputs become available or delete this request if they are lost",
			//								job.getUid().toString());
			//			} else {
			//				warningMessage = "";
			//			}
			
			if (properties.isProductTypeEstimationEnabled()) {
				LOGGER.debug("output product type estimation enabled");
				outputEstimation.estimateWithoutError();
			} else {
				LOGGER.debug("output product type estimation disabled");
			}
			
			return catalogJobs;
		} catch (Exception e) {
			if (properties.isProductTypeEstimationEnabled()) {
				LOGGER.debug("output product type estimation enabled");
				outputEstimation.estimateWithError();
			} else {
				LOGGER.debug("output product type estimation disabled");
			}
			WorkingDirectoryUtils workingDirUtils = new WorkingDirectoryUtils(obsClient, properties.getHostname());
			workingDirUtils.copyWorkingDirectory(reporting, reporting.getUid(), job, ProductFamily.FAILED_WORKDIR);
			throw e;
		} finally {
			cleanJobProcessing(job, poolProcessing, procExecutorSrv);
		}
	}

	private ReportingOutput toReportingOutput(final List<Message<CatalogJob>> out, final boolean debug, final Date lastInputAvailable) {
		final List<ReportingFilenameEntry> reportingEntries = out.stream()
				.map(m -> new ReportingFilenameEntry(m.getPayload().getProductFamily(),
						new File(m.getPayload().getProductName()).getName()))
				.collect(Collectors.toList());
		return new IpfFilenameReportingOutput(new ReportingFilenameEntries(reportingEntries), debug, lastInputAvailable);
	}

	/**
	 * Get the prefix for monitor logs according the step for this class instance
	 * 
	 */
	protected String getPrefixMonitorLog(final String step, final IpfExecutionJob job) {
		return MonitorLogUtils.getPrefixMonitorLog(step, job);
	}

	/**
	 * Check if thread interrupted
	 * 
	 */
	protected void checkThreadInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Current thread is interrupted");
		}
	}

	/**
	 * Wait for the processes execution completion
	 */
	// TODO FIXME this needs to be cleaned up to have a self contained cancellation
	// process
	protected void waitForPoolProcessesEnding(final String message, final Future<?> submittedFuture,
			final ExecutorCompletionService<Void> procCompletionSrv, final long timeoutMilliSeconds)
			throws InterruptedException, AbstractCodedException {
		try {
			checkThreadInterrupted();
			final Future<?> future = procCompletionSrv.poll(timeoutMilliSeconds, TimeUnit.MILLISECONDS);
			// timeout scenario
			if (future == null) {
				submittedFuture.cancel(true);
				throw new InterruptedException();
			}
			if (future.isCancelled()) {
				LOGGER.debug("{}: cancelled", message);
				throw new InterruptedException();
			}
			future.get();
			LOGGER.debug("{}: successfully executed", message);
		} catch (final ExecutionException e) {
			if (e.getCause() instanceof AbstractCodedException) {
				throw (AbstractCodedException) e.getCause();
			} else {
				throw new InternalErrorException(e.getMessage(), e);
			}
		}
		// timeout scenario:
		catch (final InterruptedException e) {
			final String errMess = String.format("%s: Timeout after %s seconds", message,
					properties.getTmProcAllTasksS());

			LOGGER.debug(errMess);
			throw new InternalErrorException(errMess, e);
		}
	}

	protected void cleanJobProcessing(final IpfExecutionJob job, final boolean poolProcessing,
			final ExecutorService procExecutorSrv) {
		if (poolProcessing) {
			procExecutorSrv.shutdownNow();
			try {
				procExecutorSrv.awaitTermination(properties.getTmProcStopS(), TimeUnit.SECONDS);
				// TODO send kill if fails
			} catch (final InterruptedException e) {
				// Conserves the interruption
				Thread.currentThread().interrupt();
			}
		}

		eraseWorkingDirectory(properties.getWorkingDir());
	}

	private void eraseWorkingDirectory(final String workingDirectoryPath) {
		if (devProperties.getStepsActivation().getOrDefault("erasing", true)) {
			final Path workingDir = Paths.get(workingDirectoryPath);
			if (Files.exists(workingDir)) {
				try {
					LOGGER.info("Erasing local working directory '{}'", workingDir.toString());
					/*
					 * Normal file walk will raise an AccessDeniedException, e.g. when a lost and
					 * found directory does exist. This we are using an own visitor that just
					 * deleted what is possible and will ignore items it is not able to access
					 */

					Files.walkFileTree(workingDir,
							new HashSet<FileVisitOption>(Arrays.asList(FileVisitOption.FOLLOW_LINKS)),
							Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
								@Override
								public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
										throws IOException {
									Files.delete(file);
									return FileVisitResult.CONTINUE;
								}

								@Override
								public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
									return FileVisitResult.SKIP_SUBTREE;
								}

								@Override
								public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
									if (!dir.equals(workingDir)) {
										Files.delete(dir);
									}

									return FileVisitResult.CONTINUE;
								}
							});
				} catch (IOException e) {
					LOGGER.error("Failed to erase local working directory '{}: {}'", workingDir.toString(),
							e.getMessage());
					this.appStatus.setError("PROCESSING");
				}
			}

		} else {
			LOGGER.info("Erasing local working directory '{}' bypassed", workingDirectoryPath);
		}
	}

	private List<ReportingFilenameEntry> toReportFilenames(final IpfExecutionJob job) {
		return job.getInputs().stream().map(this::newEntry).collect(Collectors.toList());
	}

	private ReportingFilenameEntry newEntry(final LevelJobInputDto input) {
		return new ReportingFilenameEntry(ProductFamily.fromValue(input.getFamily()),
				new File(input.getLocalPath()).getName());
	}

	private ReportingMessage errorReportMessage(final Exception e) {
		if (e instanceof AbstractCodedException) {
			final AbstractCodedException ace = (AbstractCodedException) e;
			return new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());
		}
		if (e instanceof InterruptedException) {
			return new ReportingMessage("Interrupted job processing");
		}
		// any other Exception
		return new ReportingMessage("[code {}] {}", ErrorCode.INTERNAL_ERROR, LogUtils.toString(e));
	}

	private String extractIpfVersionFromJobOrder(final IpfExecutionJob job) {
		try {
			for (LevelJobInputDto inputDto : job.getInputs()) {
				if (ProductFamily.JOB_ORDER.equals(ProductFamily.fromValue(inputDto.getFamily()))) {
					InputStream inputStream = new ByteArrayInputStream(
							inputDto.getContentRef().getBytes(StandardCharsets.UTF_8));
					final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					final Document document = documentBuilder.parse(inputStream);
					final XPath xPath = XPathFactory.newInstance().newXPath();
					final XPathExpression xPathExpression = xPath.compile("//*[local-name()='Version']/text()");
					final Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
					return node.getNodeValue();
				}
			}
			throw new RuntimeException();
		} catch (Exception e) {
			LOGGER.warn(String.format("Could not extract IPF version from job order of job: %s", job.getUid()));
			return "not defined";
		}
	}
}
