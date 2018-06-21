package fr.viveris.s1pdgs.level0.wrapper.services.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.config.ApplicationProperties;
import fr.viveris.s1pdgs.level0.wrapper.config.DevProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ResumeDetails;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.services.file.InputDownloader;
import fr.viveris.s1pdgs.level0.wrapper.services.file.OutputProcessor;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.ObsService;
import fr.viveris.s1pdgs.level0.wrapper.services.task.PoolExecutorCallable;

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
public class JobProcessor implements Callable<Boolean> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(JobProcessor.class);

    /**
     * Identifier for default monitor logs
     */
    protected static final String LOG_DFT = "default";
    /**
     * Identifier for processes execution monitor logs
     */
    protected static final String LOG_PROCESS = "process";
    /**
     * Identifier for input download monitor logs
     */
    protected static final String LOG_INPUT = "input";
    /**
     * Identifier for output processing monitor logs
     */
    protected static final String LOG_OUTPUT = "output";
    /**
     * Identifier for erasing monitor logs
     */
    protected static final String LOG_ERASE = "erase";
    /**
     * Identifier for resuming monitor logs
     */
    protected static final String LOG_RESUME = "resume";
    /**
     * Identifier for status monitor logs
     */
    protected static final String LOG_STATUS = "status";
    /**
     * Identifier for end monitor logs
     */
    protected static final String LOG_END = "end";
    /**
     * Identifier for error monitor logs
     */
    protected static final String LOG_ERROR = "error";

    /**
     * Processed job
     */
    private final JobDto job;

    /**
     * Application status
     */
    private final AppStatus appStatus;

    /**
     * Input downloader
     */
    private final InputDownloader inputDownloader;

    /**
     * Output processsor
     */
    private final OutputProcessor outputProcessor;

    /**
     * Development properties
     */
    private final DevProperties devProperties;

    /**
     * Application properties
     */
    private final ApplicationProperties properties;

    /**
     * Level processes executor service
     */
    private final ExecutorService procExecutorSrv;

    /**
     * Level processes completion service
     */
    private final CompletionService<Boolean> procCompletionSrv;

    /**
     * Level processes executor
     */
    private final PoolExecutorCallable procExecutor;

    /**
     * Registry of kafka consumers
     */
    private final KafkaListenerEndpointRegistry kafkaRegistry;

    /**
     * Id of the consumer
     */
    private final String kafkaContainerId;

    /**
     * Id of the topic
     */
    private final String topicName;

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
    public JobProcessor(final JobDto job, final AppStatus appStatus,
            final ApplicationProperties properties,
            final DevProperties devProperties, final String kafkaContainerId,
            final String topicName,
            final KafkaListenerEndpointRegistry kafkaRegistry,
            final ObsService obsService,
            final OutputProcuderFactory procuderFactory,
            final String outputListFile) {
        this.job = job;
        this.appStatus = appStatus;
        this.devProperties = devProperties;
        this.properties = properties;
        this.kafkaRegistry = kafkaRegistry;
        this.kafkaContainerId = kafkaContainerId;
        this.topicName = topicName;

        // Initialize the pool processor executor
        this.procExecutor = new PoolExecutorCallable(this.properties, this.job,
                getPrefixMonitorLog(LOG_PROCESS, this.job));
        this.procExecutorSrv = Executors.newSingleThreadExecutor();
        this.procCompletionSrv =
                new ExecutorCompletionService<>(procExecutorSrv);

        // Initialize the input downloader
        inputDownloader = new InputDownloader(obsService,
                this.job.getWorkDirectory(), this.job.getInputs(),
                this.properties.getSizeBatchDownload(),
                getPrefixMonitorLog(LOG_INPUT, this.job), procExecutor,
                this.properties.getLevel());

        // Initiliaze the outpt processor
        outputProcessor = new OutputProcessor(obsService, procuderFactory,
                this.job.getWorkDirectory(), this.job.getOutputs(),
                outputListFile, this.properties.getSizeBatchUpload(),
                getPrefixMonitorLog(LOG_OUTPUT, this.job));

    }

    /**
     * Constructor used for tests
     * 
     * @param job
     * @param appStatus
     * @param properties
     * @param devProperties
     * @param kafkaContainerId
     * @param kafkaRegistry
     * @param inputDownloader
     * @param outputProcessor
     * @param procExecutor
     */
    protected JobProcessor(final JobDto job, final AppStatus appStatus,
            final ApplicationProperties properties,
            final DevProperties devProperties, final String kafkaContainerId,
            final String topicName,
            final KafkaListenerEndpointRegistry kafkaRegistry,
            final InputDownloader inputDownloader,
            final OutputProcessor outputProcessor,
            final PoolExecutorCallable procExecutor) {
        super();

        this.job = job;
        this.appStatus = appStatus;
        this.devProperties = devProperties;
        this.properties = properties;
        this.kafkaRegistry = kafkaRegistry;
        this.kafkaContainerId = kafkaContainerId;
        this.topicName = topicName;
        this.procExecutor = procExecutor;
        this.inputDownloader = inputDownloader;
        this.outputProcessor = outputProcessor;
        this.procExecutorSrv = Executors.newSingleThreadExecutor();
        this.procCompletionSrv =
                new ExecutorCompletionService<>(procExecutorSrv);
    }

    /**
     * Get the prefix for monitor logs according the step
     * 
     * @param step
     * @return
     */
    private static String getPrefixMonitorLog(final String step,
            final JobDto job) {
        String ret;
        switch (step) {
            case LOG_ERROR:
                ret = String.format("[productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_PROCESS:
                ret = String.format(
                        "[MONITOR] [step 3] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_INPUT:
                ret = String.format(
                        "[MONITOR] [step 2] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_OUTPUT:
                ret = String.format(
                        "[MONITOR] [step 4] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_ERASE:
                ret = String.format(
                        "[MONITOR] [step 5] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_RESUME:
                ret = String.format(
                        "[MONITOR] [step 7] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_STATUS:
                ret = String.format(
                        "[MONITOR] [step 6] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_END:
                ret = String.format(
                        "[MONITOR] [step 0] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            default:
                ret = "[MONITOR]";
                break;
        }
        return ret;
    }

    /**
     * Get the prefix for monitor logs according the step for this class
     * instance
     * 
     * @param step
     * @return
     */
    protected String getPrefixMonitorLog(final String step) {
        return getPrefixMonitorLog(step, job);
    }

    /**
     * Execute a job
     */
    @Override
    public Boolean call() throws Exception {
        int step = 2;
        boolean poolProcessorInProgress = false;

        try {
            step = 3;
            if (devProperties.getStepsActivation().get("execution")) {
                this.processPoolProcesses();
                poolProcessorInProgress = true;
            } else {
                LOGGER.info("{} Executing processes bypassed",
                        getPrefixMonitorLog(LOG_PROCESS));
            }

            step = 2;
            if (devProperties.getStepsActivation().get("download")) {
                this.processInputs();
            } else {
                LOGGER.info("{} Preparing local working directory bypassed",
                        getPrefixMonitorLog(LOG_INPUT));
            }

            step++;
            if (devProperties.getStepsActivation().get("execution")) {
                this.waitForPoolProcessesEnding();
                poolProcessorInProgress = false;
            } else {
                LOGGER.info("{} Executing processes bypassed",
                        getPrefixMonitorLog(LOG_PROCESS));
            }

            step++;
            if (devProperties.getStepsActivation().get("upload")) {
                this.processOutputs();
            } else {
                LOGGER.info("{} Processing l0 outputs bypasssed",
                        getPrefixMonitorLog(LOG_OUTPUT));
            }

        } catch (AbstractCodedException e) {
            // Log occurred error
            LOGGER.error("{} [step {}] {} [resuming {}] [code {}] {}",
                    getPrefixMonitorLog(LOG_DFT), step,
                    new ResumeDetails(topicName, job),
                    getPrefixMonitorLog(LOG_ERROR), e.getCode().getCode(),
                    e.getLogMessage());
            this.appStatus.setError();

        } finally {

            if (poolProcessorInProgress) {
                this.terminateProcessesExecution();
            }

            if (devProperties.getStepsActivation().get("erasing")) {
                this.eraseLocalDirectory();
            } else {
                LOGGER.info("{} Erasing local working directory bypassed",
                        getPrefixMonitorLog(LOG_ERASE));
            }

            if (this.checkingStatus()) {
                this.resumeConsumer();
            }
        }

        LOGGER.info("{} End L0 job generation", getPrefixMonitorLog(LOG_END));

        return true;
    }

    /**
     * Check if thread interrupted
     * 
     * @throws InterruptedException
     */
    private void checkThreadInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Current thread is interrupted");
        }
    }

    /**
     * Launch the processes execution
     */
    protected void processPoolProcesses() {
        LOGGER.info("{} Starting process executor",
                getPrefixMonitorLog(LOG_PROCESS));
        procCompletionSrv.submit(procExecutor);
    }

    /**
     * Wait for the processes execution completion
     * 
     * @throws InterruptedException
     * @throws AbstractCodedException
     */
    protected void waitForPoolProcessesEnding()
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
     * Shutdown the processes execution
     */
    protected void terminateProcessesExecution() {
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

    /**
     * Download inputs
     * 
     * @throws AbstractCodedException
     * @throws InterruptedException
     */
    protected void processInputs()
            throws AbstractCodedException, InterruptedException {
        checkThreadInterrupted();
        LOGGER.info("{} Preparing local working directory",
                getPrefixMonitorLog(LOG_INPUT));
        inputDownloader.processInputs();
    }

    /**
     * Process outputs
     * 
     * @throws InterruptedException
     * @throws AbstractCodedException
     */
    protected void processOutputs()
            throws InterruptedException, AbstractCodedException {
        checkThreadInterrupted();
        LOGGER.info("{} Processing l0 outputs",
                getPrefixMonitorLog(LOG_OUTPUT));
        outputProcessor.processOutput();
    }

    /**
     * Erase the working directory
     */
    protected void eraseLocalDirectory() {
        try {
            LOGGER.info("{} Erasing local working directory",
                    getPrefixMonitorLog(LOG_ERASE));
            Path p = Paths.get(job.getWorkDirectory());
            Files.walk(p, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder()).map(Path::toFile)
                    .peek(System.out::println).forEach(File::delete);
        } catch (IOException e) {
            LOGGER.error("{} [code {}] Failed to erase local working directory",
                    getPrefixMonitorLog(LOG_ERASE),
                    ErrorCode.INTERNAL_ERROR.getCode());
            this.appStatus.setError();
        }
    }

    /**
     * Resume the consumer
     */
    protected void resumeConsumer() {
        LOGGER.info("{} Resuming consumer", getPrefixMonitorLog(LOG_RESUME));
        if (kafkaRegistry.getListenerContainer(kafkaContainerId) != null) {
            kafkaRegistry.getListenerContainer(kafkaContainerId).resume();
        } else {
            LOGGER.info(
                    "{} [code {}] Cannot resume consumer because no listener {}",
                    getPrefixMonitorLog(LOG_RESUME),
                    ErrorCode.KAFKA_RESUMING_ERROR.getCode(), kafkaContainerId);
        }
    }

    /**
     * Check status and stop if needed
     * 
     * @return
     */
    protected boolean checkingStatus() {
        LOGGER.info("{} Checking status consumer",
                getPrefixMonitorLog(LOG_STATUS));
        if (appStatus.getStatus().isStopping()) {
            System.exit(0);
        } else if (appStatus.getStatus().isFatalError()) {
            System.exit(-1);
        } else {
            appStatus.setWaiting();
            return true;
        }
        return false;
    }
}
