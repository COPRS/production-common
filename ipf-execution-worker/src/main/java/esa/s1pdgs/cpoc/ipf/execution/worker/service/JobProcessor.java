package esa.s1pdgs.cpoc.ipf.execution.worker.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.DevProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.MonitorLogUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.InputDownloader;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.OutputProcessor;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.ipf.execution.worker.service.report.IpfFilenameReportingOutput;
import esa.s1pdgs.cpoc.ipf.execution.worker.service.report.JobReportingInput;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
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
@Service
public class JobProcessor implements MqiListener<IpfExecutionJob> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(JobProcessor.class);

	/**
	 * Application status
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
	private final OutputProcuderFactory procuderFactory;

	/**
	 * Output processsor
	 */
	private final ObsClient obsClient;

	/**
	 * MQI service for reading message
	 */
	private final MqiClient mqiClient;

	private final List<MessageFilter> messageFilter;
	/**
	 * MQI service for stopping the MQI
	 */
	private final StatusService mqiStatusService;

	private final ErrorRepoAppender errorAppender;
	
	private final long pollingIntervalMs;
	
	private final long initDelayPollMs;
	
	/**
	 */
	@Autowired
	public JobProcessor(
			final AppStatus appStatus, 
			final ApplicationProperties properties,
			final DevProperties devProperties, 
			final ObsClient obsClient, 
			final OutputProcuderFactory procuderFactory,
			final GenericMqiClient mqiClient, 
			final List<MessageFilter> messageFilter,
			final ErrorRepoAppender errorAppender,
			final StatusService mqiStatusService,
			@Value("${process.init-delay-poll-ms}") final long initDelayPollMs,
			@Value("${process.fixed-delay-ms}") final long pollingIntervalMs
	) {
		this.appStatus = appStatus;
		this.devProperties = devProperties;
		this.properties = properties;
		this.obsClient = obsClient;
		this.procuderFactory = procuderFactory;
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.mqiStatusService = mqiStatusService;
		this.errorAppender = errorAppender;
		this.initDelayPollMs = initDelayPollMs;
		this.pollingIntervalMs = pollingIntervalMs;
	}
	
	@PostConstruct
	public void initService() {
		// allow disabling polling (e.g. for junit test) by configuring the polling interval
		if (pollingIntervalMs > 0L) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<>(
					mqiClient,
					ProductCategory.LEVEL_JOBS,
					this,
					messageFilter,
					pollingIntervalMs,
					initDelayPollMs,
					appStatus
			));
		}
	}


	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<IpfExecutionJob> message) {
		LOGGER.info("Initializing job processing {}", message);

		// ----------------------------------------------------------
		// Initialize processing
		// ------------------------------------------------------
		final IpfExecutionJob job = message.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
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
			outputListFile = job.getWorkDirectory() + "product.LIST";
			category = ProductCategory.S3_PRODUCTS;
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
				this.properties.getSizeBatchDownload(), 
				getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job),
				procExecutor, 
				this.properties.getLevel(),
				this.properties.getPathJobOrderXslt()
		);

		final OutputProcessor outputProcessor = new OutputProcessor(obsClient, procuderFactory, message, outputListFile,
				this.properties.getSizeBatchUpload(), getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job),
				this.properties.getLevel(), properties);
		
		
		reporting.begin(
				JobReportingInput.newInstance(toReportFilenames(job), jobOrderName),	
				new ReportingMessage("Start job processing")
		);
		return new MqiMessageEventHandler.Builder<ProductionEvent>(category)
				.onSuccess(res -> reporting.end(toReportingOutput(res, job.isDebug()), new ReportingMessage("End job processing")))
				.onWarning(res -> reporting.warning(toReportingOutput(res, job.isDebug()), new ReportingMessage("End job processing")))
				.onError(e -> reporting.error(errorReportMessage(e)))
				.publishMessageProducer(() -> processJob(message, inputDownloader, outputProcessor, procExecutorSrv, procCompletionSrv, procExecutor, reporting))
				.newResult();
	}

	@Override
	public void onTerminalError(final GenericMessageDto<IpfExecutionJob> message, final Exception error) {
        LOGGER.error(error);
        
        final FailedProcessingDto failedProcessing = new FailedProcessingDto(
        		properties.getHostname(),
        		new Date(), 
        		String.format("Error on handling IpfExecutionJob message %s: %s", message.getId(), LogUtils.toString(error)), 
        		message
        );
        // FIXME: workaraound for retry counter, shall only be increased when restarting or reevaluating
        message.getBody().getIpfPreparationJobMessage().getBody().increaseRetryCounter();
        failedProcessing.setPredecessor(message.getBody().getIpfPreparationJobMessage());
        errorAppender.send(failedProcessing);
		exitOnAppStatusStopOrWait();
	}
	
	@Override
	public void onWarning(final GenericMessageDto<IpfExecutionJob> message, final String warningMessage) {
		LOGGER.warn(warningMessage);
				
		final FailedProcessingDto failedProcessing = new FailedProcessingDto(
        		properties.getHostname(),
        		new Date(), 
        		String.format("Warning on handling IpfExecutionJob message %s: %s", message.getId(), warningMessage), 
        		message
        );
		// FIXME: workaraound for retry counter, shall only be increased when restarting or reevaluating
		message.getBody().getIpfPreparationJobMessage().getBody().increaseRetryCounter();
        failedProcessing.setPredecessor(message.getBody().getIpfPreparationJobMessage());
        errorAppender.send(failedProcessing);
	}
	
    protected MqiPublishingJob<ProductionEvent> processJob(
    		final GenericMessageDto<IpfExecutionJob> message,
            final InputDownloader inputDownloader,
            final OutputProcessor outputProcessor,
            final ExecutorService procExecutorSrv,
            final ExecutorCompletionService<Void> procCompletionSrv,
            final PoolExecutorCallable procExecutor,
            final Reporting reporting /* TODO: Refactor to not expect an already begun reporting... */) throws Exception {
        boolean poolProcessing = false;
        final IpfExecutionJob job = message.getBody();
        final List<GenericPublicationMessageDto<? extends AbstractMessage>> productionEvents = new ArrayList<>();
        
        try {
            LOGGER.info("{} Starting process executor",
                    getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS, job));
            procCompletionSrv.submit(procExecutor);
            poolProcessing = true;
            
            final List<ObsDownloadObject> downloadToBatch;
            if (devProperties.getStepsActivation().get("download")) {
                checkThreadInterrupted();
                LOGGER.info("{} Preparing local working directory",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job));
                downloadToBatch = inputDownloader.processInputs(reporting);
            } else {
                LOGGER.info("{} Preparing local working directory bypassed",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job));
                downloadToBatch = Collections.emptyList();
            }
            this.waitForPoolProcessesEnding(procCompletionSrv);
            poolProcessing = false;
            
            if (devProperties.getStepsActivation().get("upload")) {
                checkThreadInterrupted();
                LOGGER.info("{} Processing l0 outputs",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
                productionEvents.addAll(outputProcessor.processOutput(reporting, reporting.getUid()));
            } else {
                LOGGER.info("{} Processing l0 outputs bypasssed",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
            }
            
            // If there was a missing chunk, we submit a warning in order to deal with the
            // missing chunk and operator needs to decide, whether to restart it or delete it.
            
            final List<String> missingChunks = downloadToBatch.stream()
            	.filter(o -> o.getFamily() == ProductFamily.INVALID)
            	.map(ObsObject::getKey)
            	.collect(Collectors.toList());
            
            final String warningMessage;            
            if (missingChunks.isEmpty()) {
            	warningMessage = String.format(
        				"Missing RAWs detected for successful production %s: %s. "
        				+ "Restart if chunks become available or delete this request if they are lost", 
        				message.getId(), 
        				missingChunks
            	); 
            }
            else if (job.isTimedOut()) {
            	warningMessage = String.format(
        				"JobGeneration timed out before successful production %s. "
        				+ "Restart if missing inputs become available or delete this request if they are lost", 
        				message.getId()
            	); 
            }
            else {
            	warningMessage = "";
            }
            return new MqiPublishingJob<>(productionEvents, warningMessage);
		} finally {
            cleanJobProcessing(job, poolProcessing, procExecutorSrv);
        }
    }
    
	private ReportingOutput toReportingOutput(final List<GenericPublicationMessageDto<ProductionEvent>> out, final boolean debug) {
		final List<ReportingFilenameEntry> reportingEntries = out.stream()
				.map(m -> new ReportingFilenameEntry(m.getFamily(), new File(m.getDto().getProductName()).getName()))
				.collect(Collectors.toList());		
		return new IpfFilenameReportingOutput(new ReportingFilenameEntries(reportingEntries), debug);
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
	 * 
	 */
	protected void waitForPoolProcessesEnding(final ExecutorCompletionService<Void> procCompletionSrv)
			throws InterruptedException, AbstractCodedException {
		checkThreadInterrupted();
		try {
			procCompletionSrv.take().get(properties.getTmProcAllTasksS(), TimeUnit.SECONDS);
		} catch (final ExecutionException e) {
			if (e.getCause() instanceof AbstractCodedException) {
				throw (AbstractCodedException) e.getCause();
			} else {
				throw new InternalErrorException(e.getMessage(), e);
			}
		} catch (final TimeoutException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	/**
	 */
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
		if (devProperties.getStepsActivation().get("erasing")) {
			final Path workingDir = Paths.get(workingDirectoryPath);
			if (Files.exists(workingDir)) {
				try {
					LOGGER.info("Erasing local working directory '{}'", workingDir.toString());
					// TODO: possible candidate to use instead, if dumping of deleted files not required: FileUtils.delete(workingDir.toString());
	                Files.walk(workingDir, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder()).map(Path::toFile)
                    .forEach(File::delete);
				} catch (final IOException e) {
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
		return job.getInputs().stream()
			.map(this::newEntry)
			.collect(Collectors.toList());
	}
	
	private ReportingFilenameEntry newEntry(final LevelJobInputDto input) {
		return new ReportingFilenameEntry(
				ProductFamily.fromValue(input.getFamily()),
				new File(input.getLocalPath()).getName()
		);
	}

	// checks AppStatus, whether app shall be stopped and in that case, shut down this service as well
	private void exitOnAppStatusStopOrWait() {
		if (appStatus.getStatus().isStopping()) {
			// TODO send stop to the MQI
			try {
				mqiStatusService.stop();
			} catch (final AbstractCodedException ace) {
				LOGGER.error("MQI service couldn't be stopped", ace);
			}
			appStatus.setShallBeStopped(true);
			appStatus.forceStopping(); // only stops when isShallBeStopped() == true
		} else if (appStatus.getStatus().isFatalError()) {
			appStatus.setShallBeStopped(true);
			appStatus.forceStopping(); // only stops when isShallBeStopped() == true
		} else {
			appStatus.setWaiting();
		}
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
	
	
}
