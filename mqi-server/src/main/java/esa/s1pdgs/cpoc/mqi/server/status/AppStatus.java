package esa.s1pdgs.cpoc.mqi.server.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.AppState;

/**
 * Application status
 * 
 * @author Viveris Technologies
 */
@Component
public class AppStatus {

    /**
     * Status
     */
    private final MqiServerStatus status;

    /**
     * Maximal number of consecutive errors
     */
    private final int maxErrorCounter;

    /**
     * Indicate if the application shall be stopped
     */
    private boolean shallBeStopped;

    /**
     * Constructor
     * 
     * @param maxErrorCounter
     */
    @Autowired
    public AppStatus(
            @Value("${application.max-error-counter}") final int maxErrorCounter) {
        this.status = new MqiServerStatus();
        this.shallBeStopped = false;
        this.maxErrorCounter = maxErrorCounter;
    }

    /**
     * @return the status
     */
    public synchronized MqiServerStatus getStatus() {
        return status;
    }

    /**
     * Set application as waiting
     */
    public synchronized void resetError() {
        this.status.resetError();
    }

    /**
     * Set application as stopping
     */
    public synchronized void setStopping() {
        this.setShallBeStopped(true);
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
    public class MqiServerStatus {

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
        public MqiServerStatus() {
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
         * Set status PROCESSING
         */
        public void resetError() {
            if (!isStopping() && !isFatalError()) {
                state = AppState.WAITING;
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
    @Scheduled(fixedDelayString = "${application.stop-fixed-delay-ms}")
    public void forceStopping() {
        if (this.isShallBeStopped()) {
            System.exit(0);
        }
    }
}
