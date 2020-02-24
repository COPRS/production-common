package esa.s1pdgs.cpoc.ipf.execution.worker.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.DevProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.InputDownloader;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.file.OutputProcessor;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.JobOrderReportingInput;

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

	/**
	 * MQI service for stopping the MQI
	 */
	private final StatusService mqiStatusService;

	private final ErrorRepoAppender errorAppender;
	
	private final long pollingIntervalMs;
	
	private final long initDelayPollMs;


	/**
	 * @param job
	 * @param appStatus
	 * @param properties
	 * @param devProperties
	 * @param kafkaContainerId
	 * @param kafkaRegistry
	 * @param obsClient
	 * @param procuderFactory
	 * @param outputListFile
	 */
	@Autowired
	public JobProcessor(
			final AppStatus appStatus, 
			final ApplicationProperties properties,
			final DevProperties devProperties, 
			final ObsClient obsClient, 
			final OutputProcuderFactory procuderFactory,
			final GenericMqiClient mqiClient, 
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
			service.execute(new MqiConsumer<IpfExecutionJob>(
					mqiClient,
					ProductCategory.LEVEL_JOBS, 
					this, 
					pollingIntervalMs,
					initDelayPollMs,
					appStatus
			));
		}
	}


	@Override
	public final void onMessage(final GenericMessageDto<IpfExecutionJob> message) throws Exception {
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
		final String jobOrderName = new File(job.getJobOrder()).getName();
		reporting.begin(
				new JobOrderReportingInput(toReportFilenames(job), jobOrderName, Collections.emptyMap()),				
				new ReportingMessage("Start job processing")
		);

		final File workdir = new File(job.getWorkDirectory());
		// Clean up the working directory with all of its content
		eraseWorkingDirectory(properties.getWorkingDir());

		// Build output list filename
		String outputListFile = job.getWorkDirectory() + workdir.getName() + ".LIST";
		if (properties.getLevel() == ApplicationLevel.L0) {
			outputListFile = job.getWorkDirectory() + "AIOProc.LIST";
		} else if (properties.getLevel() == ApplicationLevel.L0_SEGMENT) {
			outputListFile = job.getWorkDirectory() + "L0ASProcList.LIST";
		}
		LOGGER.debug("Output list build {}", outputListFile);

		final PoolExecutorCallable procExecutor = new PoolExecutorCallable(properties, job,
				getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS, job), this.properties.getLevel(), reporting);
		
		final ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
		final ExecutorCompletionService<Void> procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);
		final InputDownloader inputDownloader = new InputDownloader(obsClient, job.getWorkDirectory(), job.getInputs(),
				this.properties.getSizeBatchDownload(), getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job),
				procExecutor, this.properties.getLevel());

		final OutputProcessor outputProcessor = new OutputProcessor(obsClient, procuderFactory, message, outputListFile,
				this.properties.getSizeBatchUpload(), getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job),
				this.properties.getLevel(), properties);

		processJob(message, inputDownloader, outputProcessor, procExecutorSrv, procCompletionSrv, procExecutor, reporting);
	}

	@Override
	public void onTerminalError(final GenericMessageDto<IpfExecutionJob> message, final Exception error) {
        LOGGER.error(error);  
        errorAppender.send(new FailedProcessingDto(
        		properties.getHostname(),
        		new Date(), 
        		String.format("Error on handling IpfExecutionJob message %s: %s", message.getId(), LogUtils.toString(error)), 
        		message
        ));
		exitOnAppStatusStopOrWait();
	}
	
    protected void processJob(final GenericMessageDto<IpfExecutionJob> message,
            final InputDownloader inputDownloader,
            final OutputProcessor outputProcessor,
            final ExecutorService procExecutorSrv,
            final ExecutorCompletionService<Void> procCompletionSrv,
            final PoolExecutorCallable procExecutor,
            final Reporting reporting /* TODO: Refactor to not expect an already begun reporting... */) throws Exception {
        boolean poolProcessing = false;
        final IpfExecutionJob job = message.getBody();
        
        ReportingOutput reportingOutput = ReportingOutput.NULL;
        
        try {
            LOGGER.info("{} Starting process executor",
                    getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS, job));
            procCompletionSrv.submit(procExecutor);
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
            this.waitForPoolProcessesEnding(procCompletionSrv);
            poolProcessing = false;
            
            if (devProperties.getStepsActivation().get("upload")) {
                checkThreadInterrupted();
                LOGGER.info("{} Processing l0 outputs",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
                reportingOutput = outputProcessor.processOutput(reporting, reporting.getUid());
            } else {
                LOGGER.info("{} Processing l0 outputs bypasssed",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
            }
            reporting.end(reportingOutput, new ReportingMessage("End job processing"));
        } catch (final Exception e) {
            reporting.error(errorReportMessage(e)); 
            throw e;
		} finally {
            cleanJobProcessing(job, poolProcessing, procExecutorSrv);
        }
    }
    
    

	/**
	 * Get the prefix for monitor logs according the step for this class instance
	 * 
	 * @param step
	 * @return
	 */
	protected String getPrefixMonitorLog(final String step, final IpfExecutionJob job) {
		return MonitorLogUtils.getPrefixMonitorLog(step, job);
	}
	
	/**
	 * Check if thread interrupted
	 * 
	 * @throws InterruptedException
	 */
	protected void checkThreadInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Current thread is interrupted");
		}
	}

	/**
	 * Wait for the processes execution completion
	 * 
	 * @throws InterruptedException
	 * @throws AbstractCodedException
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
	 * @param job
	 * @param poolProcessing
	 * @param procExecutorSrv
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
                    .peek(System.out::println).forEach(File::delete);
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
	
	private final List<String> toReportFilenames(final IpfExecutionJob job) {
		return job.getInputs().stream()
			.map(j -> Paths.get(j.getLocalPath()).getFileName().toString())
			.collect(Collectors.toList());
	}
	
	// checks AppStatus, whether app shall be stopped and in that case, shut down this service as well
	private final void exitOnAppStatusStopOrWait() {
		if (appStatus.getStatus().isStopping()) {
			// TODO send stop to the MQI
			try {
				mqiStatusService.stop();
			} catch (final AbstractCodedException ace) {
				LOGGER.error("MQI service couldn't be stopped {}", ace);
			}
			System.exit(0);
		} else if (appStatus.getStatus().isFatalError()) {
			System.exit(-1);
		} else {
			appStatus.setWaiting();
		}
	}
	
	private final ReportingMessage errorReportMessage(final Exception e) {
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
