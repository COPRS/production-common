package fr.viveris.s1pdgs.jobgenerator.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

/**
 * Application status
 * 
 * @author Viveris Technologies
 */
@Component
public class AppStatus {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AppStatus.class);

    /**
     * Status
     */
    private final JobStatus status;

    /**
     * Maximal number of consecutive errors
     */
    private final int maxErrorCounter;

    /**
     * Indicate if the application shall be stopped
     */
    private boolean shallBeStopped;

    /**
     * MQI service for stopping the MQI
     */
    private final StatusService mqiStatusService;

    /**
     * Identifier of the processing message
     */
    private long processingMsgId;

    /**
     * Constructor
     * 
     * @param maxErrorCounter
     */
    @Autowired
    public AppStatus(
            @Value("${status.max-error-counter}") final int maxErrorCounter,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService) {
        this.status = new JobStatus();
        this.shallBeStopped = false;
        this.maxErrorCounter = maxErrorCounter;
        this.mqiStatusService = mqiStatusService;
        this.processingMsgId = 0;
    }

    /**
     * @return the status
     */
    public synchronized JobStatus getStatus() {
        return status;
    }

    /**
     * @return the processingMsgId
     */
    public long getProcessingMsgId() {
        return processingMsgId;
    }

    /**
     * Set application as waiting
     */
    public synchronized void setWaiting() {
        this.processingMsgId = 0;
        this.status.setWaiting();
    }

    /**
     * Set application as processing
     */
    public synchronized void setProcessing(final long processingMsgId) {
        this.processingMsgId = processingMsgId;
        this.status.setProcessing();
    }

    /**
     * Set application as stopping
     */
    public synchronized void setStopping() {
        if (!this.status.isProcessing()) {
            this.setShallBeStopped(true);
        }
        this.status.setStopping();
    }

    /**
     * Set application as error
     */
    public synchronized void setError() {
        this.status.setError(maxErrorCounter);
    }

    /**
     * @return the shallBeStopped
     */
    public synchronized boolean isShallBeStopped() {
        return shallBeStopped;
    }

    /**
     * @param shallBeStopped
     *            the shallBeStopped to set
     */
    public synchronized void setShallBeStopped(final boolean shallBeStopped) {
        this.shallBeStopped = shallBeStopped;
    }

    /**
     * Internal status
     * 
     * @author Viveris Technologies
     */
    public class JobStatus {

        /**
         * State
         */
        private AppState state;

        /**
         * Date of the last change of the status (old status != new status)
         */
        private long dateLastChangeMs;

        /**
         * Number of consecutive errors
         */
        private int errorCounter;

        /**
         * Constrcutor
         */
        public JobStatus() {
            this.state = AppState.WAITING;
            errorCounter = 0;
            dateLastChangeMs = System.currentTimeMillis();
        }

        /**
         * @return the status
         */
        public AppState getState() {
            return state;
        }

        /**
         * @return the timeSinceLastChange
         */
        public long getDateLastChangeMs() {
            return dateLastChangeMs;
        }

        /**
         * @return the errorCounter
         */
        public int getErrorCounter() {
            return errorCounter;
        }

        /**
         * Set status WAITING
         */
        public void setWaiting() {
            if (!isStopping() && !isFatalError()) {
                state = AppState.WAITING;
                dateLastChangeMs = System.currentTimeMillis();
            }
        }

        /**
         * Set status PROCESSING
         */
        public void setProcessing() {
            if (!isStopping() && !isFatalError()) {
                state = AppState.PROCESSING;
                dateLastChangeMs = System.currentTimeMillis();
                errorCounter = 0;
            }
        }

        /**
         * Set status STOPPING
         */
        public void setStopping() {
            state = AppState.STOPPING;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounter = 0;
        }

        /**
         * Set status ERROR
         */
        public void setError(final int maxErrorCounter) {
            if (!isStopping()) {
                state = AppState.ERROR;
                dateLastChangeMs = System.currentTimeMillis();
                errorCounter++;
                if (errorCounter >= maxErrorCounter) {
                    setFatalError();
                }
            }
        }

        /**
         * Set status FATALERROR
         */
        public void setFatalError() {
            state = AppState.FATALERROR;
            dateLastChangeMs = System.currentTimeMillis();
        }

        /**
         * Indicate if state is waiting
         * 
         * @return
         */
        public boolean isWaiting() {
            return state == AppState.WAITING;
        }

        /**
         * Indicate if state is PROCESSING
         * 
         * @return
         */
        public boolean isProcessing() {
            return state == AppState.PROCESSING;
        }

        /**
         * Indicate if state is STOPPING
         * 
         * @return
         */
        public boolean isStopping() {
            return state == AppState.STOPPING;
        }

        /**
         * Indicate if state is ERROR
         * 
         * @return
         */
        public boolean isError() {
            return state == AppState.ERROR;
        }

        /**
         * Indicate if state is FATALERROR
         * 
         * @return
         */
        public boolean isFatalError() {
            return state == AppState.FATALERROR;
        }

    }

    /**
     * Stop the application if someone asks for forcing stop
     */
    @Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms}")
    public void forceStopping() {
        if (this.isShallBeStopped()) {
            try {
                mqiStatusService.stop();
            } catch (AbstractCodedException ace) {
                LOGGER.error(ace.getLogMessage());
            }
            System.exit(0);
        }
    }
}
