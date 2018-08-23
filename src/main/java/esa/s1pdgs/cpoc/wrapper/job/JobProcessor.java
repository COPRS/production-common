package esa.s1pdgs.cpoc.wrapper.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.wrapper.config.ApplicationProperties;
import esa.s1pdgs.cpoc.wrapper.config.DevProperties;
import esa.s1pdgs.cpoc.wrapper.job.file.InputDownloader;
import esa.s1pdgs.cpoc.wrapper.job.file.OutputProcessor;
import esa.s1pdgs.cpoc.wrapper.job.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.wrapper.job.obs.ObsService;
import esa.s1pdgs.cpoc.wrapper.job.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.wrapper.status.AppStatus;

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
public class JobProcessor {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(JobProcessor.class);

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
    private final ObsService obsService;

    /**
     * MQI service for reading message
     */
    private final GenericMqiService<LevelJobDto> mqiService;

    /**
     * MQI service for stopping the MQI
     */
    private final StatusService mqiStatusService;

    /**
     * @param job
     * @param appStatus
     * @param properties
     * @param devProperties
     * @param kafkaContainerId
     * @param kafkaRegistry
     * @param obsService
     * @param procuderFactory
     * @param outputListFile
     */
    @Autowired
    public JobProcessor(final AppStatus appStatus,
            final ApplicationProperties properties,
            final DevProperties devProperties, final ObsService obsService,
            final OutputProcuderFactory procuderFactory,
            @Qualifier("mqiServiceForLevelJobs") final GenericMqiService<LevelJobDto> mqiService,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService) {
        this.appStatus = appStatus;
        this.devProperties = devProperties;
        this.properties = properties;
        this.obsService = obsService;
        this.procuderFactory = procuderFactory;
        this.mqiService = mqiService;
        this.mqiStatusService = mqiStatusService;
    }

    /**
     * Consume and execute jobs
     */
    @Scheduled(fixedDelayString = "${process.fixed-delay-ms}", initialDelayString = "${process.init-delay-poll-ms}")
    public void processJob() {

        // ----------------------------------------------------------
        // Read Message
        // ----------------------------------------------------------
        LOGGER.trace("[MONITOR] [step 0] Waiting message");
        if (appStatus.isShallBeStopped()) {
            LOGGER.info("[MONITOR] [step 0] The wrapper shall be stopped");
            this.appStatus.forceStopping();
            return;
        }
        GenericMessageDto<LevelJobDto> message = null;
        try {
            message = mqiService.next();
        } catch (AbstractCodedException ace) {
            LOGGER.error("[MONITOR] [step 0] [code {}] {}",
                    ace.getCode().getCode(), ace.getLogMessage());
            message = null;
        }
        if (message == null || message.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }
        appStatus.setProcessing(message.getIdentifier());
        LOGGER.info("Initializing job processing {}", message);

        // ----------------------------------------------------------
        // Initialize processing
        // ------------------------------------------------------
        LevelJobDto job = message.getBody();
        LOGGER.info("[REPORT] [s1pdgsTask {}Processing] [subTask messageProcessing] {} [productName {}] [START] Start L0 Job generation",
        		properties.getLevel(), getPrefixMonitorLog(MonitorLogUtils.LOG_READ, job), job.getProductIdentifier());
        File workdir = new File(job.getWorkDirectory());
        // Remove working directory if exist
        if (workdir.exists()) {
            this.eraseDirectory(job);
        }
        // Build output list
        String outputListFile =
                job.getWorkDirectory() + workdir.getName() + ".LIST";
        if (properties.getLevel() == ApplicationLevel.L0) {
            outputListFile = job.getWorkDirectory() + "AIOProc.LIST";
        }
        // Initialize the pool processor executor
        PoolExecutorCallable procExecutor = new PoolExecutorCallable(properties,
                job, getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS, job), this.properties.getLevel());
        ExecutorService procExecutorSrv = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Boolean> procCompletionSrv =
                new ExecutorCompletionService<>(procExecutorSrv);
        // Initialize the input downloader
        InputDownloader inputDownloader =
                new InputDownloader(obsService, job.getWorkDirectory(),
                        job.getInputs(), this.properties.getSizeBatchDownload(),
                        getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job),
                        procExecutor, this.properties.getLevel());
        // Initiliaze the output processor
        OutputProcessor outputProcessor =
                new OutputProcessor(obsService, procuderFactory, message,
                        outputListFile, this.properties.getSizeBatchUpload(),
                        getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job), this.properties.getLevel());

        // ----------------------------------------------------------
        // Process message
        // ----------------------------------------------------------
        processJob(message, inputDownloader, outputProcessor, procExecutorSrv,
                procCompletionSrv, procExecutor);

        LOGGER.info("[REPORT] [s1pdgsTask {}Processing] [subTask messageProcessing {} [productName {}] [STOP] End L0 job generation",
        		properties.getLevel(), getPrefixMonitorLog(MonitorLogUtils.LOG_END, job), job.getProductIdentifier());

    }

    /**
     * Get the prefix for monitor logs according the step for this class
     * instance
     * 
     * @param step
     * @return
     */
    protected String getPrefixMonitorLog(final String step,
            final LevelJobDto job) {
        return MonitorLogUtils.getPrefixMonitorLog(step, job);
    }

    /**
     * @param job
     * @param inputDownloader
     * @param outputProcessor
     * @param procExecutorSrv
     * @param procCompletionSrv
     * @param procExecutor
     */
    protected void processJob(final GenericMessageDto<LevelJobDto> message,
            final InputDownloader inputDownloader,
            final OutputProcessor outputProcessor,
            final ExecutorService procExecutorSrv,
            final ExecutorCompletionService<Boolean> procCompletionSrv,
            final PoolExecutorCallable procExecutor) {
        boolean poolProcessing = false;
        LevelJobDto job = message.getBody();
        int step = 0;
        boolean ackOk = false;
        String errorMessage = "";
        try {
            step = 3;
            LOGGER.info("{} Starting process executor",
                    getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS, job));
            procCompletionSrv.submit(procExecutor);
            poolProcessing = true;

            step = 2;
            if (devProperties.getStepsActivation().get("download")) {
                checkThreadInterrupted();
                LOGGER.info("{} Preparing local working directory",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job));
                inputDownloader.processInputs();
            } else {
                LOGGER.info("{} Preparing local working directory bypassed",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job));
            }

            step = 3;
            this.waitForPoolProcessesEnding(procCompletionSrv);
            poolProcessing = false;

            step = 4;
            if (devProperties.getStepsActivation().get("upload")) {
                checkThreadInterrupted();
                LOGGER.info("{} Processing l0 outputs",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
                outputProcessor.processOutput();
            } else {
                LOGGER.info("{} Processing l0 outputs bypasssed",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
            }
            ackOk = true;

        } catch (AbstractCodedException ace) {
            ackOk = false;
            errorMessage = String.format("%s [step %d] %s [code %d] %s",
                    getPrefixMonitorLog(MonitorLogUtils.LOG_DFT, job), step,
                    getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR, job),
                    ace.getCode().getCode(), ace.getLogMessage());
        } catch (InterruptedException e) {
            ackOk = false;
            errorMessage = String.format(
                    "%s [step %d] %s [code %d] [msg interrupted exception]",
                    getPrefixMonitorLog(MonitorLogUtils.LOG_DFT, job), step,
                    getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR, job),
                    ErrorCode.INTERNAL_ERROR.getCode());
        } finally {
            cleanJobProcessing(job, poolProcessing, procExecutorSrv);
        }

        // Ack and check if application shall stopped
        ackProcessing(message, ackOk, errorMessage);
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
    protected void waitForPoolProcessesEnding(
            final ExecutorCompletionService<Boolean> procCompletionSrv)
            throws InterruptedException, AbstractCodedException {
        checkThreadInterrupted();
        try {
            procCompletionSrv.take().get(properties.getTmProcAllTasksS(),
                    TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AbstractCodedException) {
                throw (AbstractCodedException) e.getCause();
            } else {
                throw new InternalErrorException(e.getMessage(), e);
            }
        } catch (TimeoutException e) {
            throw new InternalErrorException(e.getMessage(), e);
        }
    }

    /**
     * @param job
     * @param poolProcessing
     * @param procExecutorSrv
     */
    protected void cleanJobProcessing(final LevelJobDto job,
            final boolean poolProcessing,
            final ExecutorService procExecutorSrv) {
        if (poolProcessing) {
            procExecutorSrv.shutdownNow();
            try {
                procExecutorSrv.awaitTermination(properties.getTmProcStopS(),
                        TimeUnit.SECONDS);
                // TODO send kill if fails
            } catch (InterruptedException e) {
                // Conserves the interruption
                Thread.currentThread().interrupt();
            }
        }
        this.eraseDirectory(job);
    }
    
    private void eraseDirectory(final LevelJobDto job) {
        if (devProperties.getStepsActivation().get("erasing")) {
            try {
                LOGGER.info("{} Erasing local working directory",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_ERASE, job));
                Path p = Paths.get(job.getWorkDirectory());
                Files.walk(p, FileVisitOption.FOLLOW_LINKS)
                        .sorted(Comparator.reverseOrder()).map(Path::toFile)
                        .peek(System.out::println).forEach(File::delete);
            } catch (IOException e) {
                LOGGER.error(
                        "{} [code {}] Failed to erase local working directory",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_ERASE, job),
                        ErrorCode.INTERNAL_ERROR.getCode());
                this.appStatus.setError();
            }
        } else {
            LOGGER.info("{} Erasing local working directory bypassed",
                    getPrefixMonitorLog(MonitorLogUtils.LOG_ERASE, job));
        }
    }

    /**
     * Ack job processing and stop app if needed
     * @param dto
     * @param ackOk
     * @param errorMessage
     */
    protected void ackProcessing(final GenericMessageDto<LevelJobDto> dto,
            final boolean ackOk, final String errorMessage) {
        boolean stopping = appStatus.getStatus().isStopping();

        // Ack
        if (ackOk) {
            ackPositively(stopping, dto);
        } else {
            ackNegatively(stopping, dto, errorMessage);
        }

        // Check status
        LOGGER.info("{} Checking status consumer",
                getPrefixMonitorLog(MonitorLogUtils.LOG_STATUS, dto.getBody()));
        if (appStatus.getStatus().isStopping()) {
            // TODO send stop to the MQI
            try {
                mqiStatusService.stop();
            } catch (AbstractCodedException ace) {
                LOGGER.error("{} {} Checking status consumer",
                        getPrefixMonitorLog(MonitorLogUtils.LOG_STATUS,
                                dto.getBody()),
                        ace.getLogMessage());
            }
            System.exit(0);
        } else if (appStatus.getStatus().isFatalError()) {
            System.exit(-1);
        } else {
            appStatus.setWaiting();
        }
    }

    /**
     * @param dto
     * @param errorMessage
     */
    protected void ackNegatively(final boolean stop,
            final GenericMessageDto<LevelJobDto> dto,
            final String errorMessage) {
        LOGGER.info("{} Acknowledging negatively",
                getPrefixMonitorLog(MonitorLogUtils.LOG_ACK, dto.getBody()));
        LOGGER.error(errorMessage);
        try {
            mqiService.ack(new AckMessageDto(dto.getIdentifier(), Ack.ERROR,
                    errorMessage, stop));
        } catch (AbstractCodedException ace) {
            LOGGER.error("{} [step 5] {} [code {}] {}",
                    getPrefixMonitorLog(MonitorLogUtils.LOG_DFT, dto.getBody()),
                    getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR,
                            dto.getBody()),
                    ace.getCode().getCode(), ace.getLogMessage());
        }
        appStatus.setError();
    }

    protected void ackPositively(final boolean stop,
            final GenericMessageDto<LevelJobDto> dto) {
        LOGGER.info("{} Acknowledging positively",
                getPrefixMonitorLog(MonitorLogUtils.LOG_ACK, dto.getBody()));
        try {
            mqiService.ack(
                    new AckMessageDto(dto.getIdentifier(), Ack.OK, null, stop));
        } catch (AbstractCodedException ace) {
            LOGGER.error("{} [step 5] {} [code {}] {}",
                    getPrefixMonitorLog(MonitorLogUtils.LOG_DFT, dto.getBody()),
                    getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR,
                            dto.getBody()),
                    ace.getCode().getCode(), ace.getLogMessage());
            appStatus.setError();
        }
    }
}
