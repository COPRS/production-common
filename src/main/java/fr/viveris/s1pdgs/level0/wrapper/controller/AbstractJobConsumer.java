package fr.viveris.s1pdgs.level0.wrapper.controller;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.config.ApplicationProperties;
import fr.viveris.s1pdgs.level0.wrapper.config.DevProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ResumeDetails;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.level0.wrapper.services.job.JobProcessor;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.ObsService;

/**
 * <p>
 * Abstraction class of a job consumer
 * </p>
 * This class consumes a job and launch a processing in a separated thread
 * before pausing kafka consumer.
 * 
 * @author Viveris Technologies
 */
public abstract class AbstractJobConsumer {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(AbstractJobConsumer.class);

    /**
     * Service for accessing to the OBS
     */
    protected final ObsService obsService;

    /**
     * Factory for producing output messages in KAFKA topics
     */
    protected final OutputProcuderFactory outputProcuder;

    /**
     * Development properties
     */
    protected final DevProperties devProperties;

    /**
     * Application properties
     */
    protected final ApplicationProperties properties;

    /**
     * Kafka endpoint registry used to pause the consumer
     */
    protected final KafkaListenerEndpointRegistry consumersRegistry;

    /**
     * Consumer identifier
     */
    protected final String springConsumerId;

    /**
     * Topic name
     */
    protected final String topic;

    /**
     * Executor service to launch th enew job processing
     */
    protected final ExecutorService jobWorkerExecutor;

    /**
     * Completion service to stop the previous job if a new message is polled
     */
    protected final CompletionService<Boolean> jobWorkerService;

    /**
     * The number of curretn job processing (0 or 1)
     */
    protected int nbCurrentTasks;

    /**
     * The application status
     */
    protected final AppStatus appStatus;

    /**
     * @param obsService
     * @param outputProcuderFactory
     * @param properties
     * @param devProperties
     * @param appStatus
     * @param kafkaListenerEndpointRegistry
     * @param listenerContainerId
     */
    public AbstractJobConsumer(final ObsService obsService,
            final OutputProcuderFactory outputProcuderFactory,
            final ApplicationProperties properties,
            final DevProperties devProperties, final AppStatus appStatus,
            final KafkaListenerEndpointRegistry consumersRegistry,
            final String springConsumerId, final String topic) {
        super();
        this.nbCurrentTasks = 0;
        this.obsService = obsService;
        this.outputProcuder = outputProcuderFactory;
        this.properties = properties;
        this.devProperties = devProperties;
        this.appStatus = appStatus;
        this.consumersRegistry = consumersRegistry;
        this.springConsumerId = springConsumerId;
        this.topic = topic;
        this.jobWorkerExecutor = Executors.newSingleThreadExecutor();
        this.jobWorkerService =
                new ExecutorCompletionService<>(this.jobWorkerExecutor);
    }

    /**
     * Message listener container. Read and process a message
     * 
     * @param job
     * @param acknowledgment
     * @param outputListFile
     */
    protected void internalReceive(final JobDto job,
            final Acknowledgment acknowledgment, final String outputListFile) {

        // Initialize logs
        String prefixLog = "[MONITOR]";
        String prefixLogStart = String.format(
                "%s [step 0] [productName %s] [workDir %s]", prefixLog,
                job.getProductIdentifier(), job.getWorkDirectory());

        LOGGER.info("{} Starting job generation", prefixLogStart);
        String step = "";

        if (appStatus.isShallBeStopped()) {
            LOGGER.info(
                    "{} End job generation: nothing done because the wrapper shall be stopped",
                    prefixLogStart);
            this.appStatus.forceStopping();

        } else {

            try {
                // Set wrapper as processing
                this.appStatus.setProcessing();

                // Ack message
                step = "1a";
                this.ackMessage(acknowledgment, String.format(
                        "%s [step 1a] [productName %s] [workDir %s]", prefixLog,
                        job.getProductIdentifier(), job.getWorkDirectory()));

                // Remove the last executed future if needed, wait for task
                // ending if necessary
                step = "1b";
                if (this.nbCurrentTasks > 0) {
                    this.cleanPreviousExecution(String.format(
                            "%s [step 1b] [productName %s] [workDir %s]",
                            prefixLog, job.getProductIdentifier(),
                            job.getWorkDirectory()));
                }

                // Launch job
                step = "1c";
                this.launchJob(job, outputListFile, String.format(
                        "%s [step 1c] [productName %s] [workDir %s]", prefixLog,
                        job.getProductIdentifier(), job.getWorkDirectory()));

                // Set the consumer in pause
                step = "1d";
                this.pauseConsumer(String.format(
                        "%s [step 1d] [productName %s] [workDir %s]", prefixLog,
                        job.getProductIdentifier(), job.getWorkDirectory()));

            } catch (Exception e) {
                LOGGER.error(
                        "{} [step {}] [productName {}] [workDir {}] [resuming {}] [code {}] Exception occurred during processing message: {}",
                        prefixLog, step, job.getProductIdentifier(),
                        job.getWorkDirectory(), new ResumeDetails(getTopic(), job),
                        ErrorCode.INTERNAL_ERROR.getCode(), e.getMessage());
                this.appStatus.setError();
            }

        }
    }

    /**
     * Acknowledge message read
     * 
     * @param acknowledgment
     * @param prefixLog
     */
    protected void ackMessage(final Acknowledgment acknowledgment,
            final String prefixLog) {
        LOGGER.info("{} Acknowledging message", prefixLog);
        try {
            acknowledgment.acknowledge();
        } catch (Exception exc) {
            LOGGER.error(
                    "{} [code {}] Exception occurred during acknowledgment {}",
                    prefixLog, ErrorCode.KAFKA_COMMIT_ERROR.getCode(),
                    exc.getMessage());
            this.appStatus.setError();
        }
    }

    /**
     * Clean the previous job processing
     * 
     * @param prefixLog
     */
    protected void cleanPreviousExecution(final String prefixLog) {
        LOGGER.info("{} Resetting worker thread pool", prefixLog);
        try {
            Future<Boolean> future = this.jobWorkerService.poll(
                    this.properties.getTmProcCheckStopS(), TimeUnit.SECONDS);
            if (future == null) {
                LOGGER.warn(
                        "{} Cannot retrieve last execution after {} seconds: force shutdown of previous job",
                        prefixLog, properties.getTmProcCheckStopS());
                this.jobWorkerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.error("{} [code {}] Exception occurred during resetting {}",
                    prefixLog, e.getMessage());
            this.appStatus.setError();
        }
        this.nbCurrentTasks = 0;
    }

    /**
     * @param job
     * @param outputListFile
     * @param prefixLog
     */
    protected void launchJob(final JobDto job, final String outputListFile,
            final String prefixLog) {
        LOGGER.info("{} Launching job in a thread", prefixLog);
        this.jobWorkerService.submit(new JobProcessor(job, this.appStatus,
                this.properties, this.devProperties, this.springConsumerId,
                consumersRegistry, obsService, outputProcuder, outputListFile));
        nbCurrentTasks++;
    }

    /**
     * Pause the consumer
     * 
     * @param prefixLog
     */
    protected void pauseConsumer(final String prefixLog) {
        LOGGER.info("{} Setting the consumer in pause", prefixLog);
        MessageListenerContainer listenerContainer = this.consumersRegistry
                .getListenerContainer(getSpringConsumerId());
        if (listenerContainer == null) {
            LOGGER.warn("{} Cannot retrieve listenerContainer", prefixLog);
        } else {
            listenerContainer.pause();
        }
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the consumerId
     */
    public String getSpringConsumerId() {
        return springConsumerId;
    }

}
