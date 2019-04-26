package esa.s1pdgs.cpoc.appcatalog.server.status;

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
    private final AppCatalogStatus status;

    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterMqi;
    
    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterJob;

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
            @Value("${status.max-error-counter-mqi}") final int maxErrorCounterMqi,
            @Value("${status.max-error-counter-job}") final int maxErrorCounterJob) {
        this.status = new AppCatalogStatus();
        this.shallBeStopped = false;
        this.maxErrorCounterMqi = maxErrorCounterMqi;
        this.maxErrorCounterJob = maxErrorCounterJob;
    }

    /**
     * @return the status
     */
    public synchronized AppCatalogStatus getStatus() {
        return status;
    }

    /**
     * Set application as waiting
     */
    public synchronized void setWaiting(String type) {
        if(!this.shallBeStopped) {
            this.status.setWaiting();
            if("MQI".equals(type)) {
                this.status.resetErrorCounterMqi();
            } else if("JOB".equals(type)) {
                this.status.resetErrorCounterJob();
            }
        }
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
    public synchronized void setError(String type) {
        if("MQI".equals(type)) {
            this.status.setErrorMqi(maxErrorCounterMqi);
        } else if("JOB".equals(type)) {
            this.status.setErrorJob(maxErrorCounterJob);
        }
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
    public class AppCatalogStatus {

        /**
         * State
         */
        private AppState state;

        /**
         * Date of the last change of the status (old status != new status)
         */
        private long dateLastChangeMs;

        /**
         * Number of consecutive errors for processing
         */
        private int errorCounterMqi;
        
        /**
         * Number of consecutive errors for processing
         */
        private int errorCounterJob;

        /**
         * Constrcutor
         */
        public AppCatalogStatus() {
            this.state = AppState.WAITING;
            errorCounterMqi = 0;
            errorCounterJob = 0;
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
        public int getErrorCounterMqi() {
            return errorCounterMqi;
        }
        
        /**
         * @return the errorCounter
         */
        public int getErrorCounterJob() {
            return errorCounterJob;
        }
        
        /**
         * Reset the error counter for MQI
         */
        public void resetErrorCounterMqi() {
            this.errorCounterMqi = 0;
        }
        
        /**
         * Reset the error counter for Job
         */
        public void resetErrorCounterJob() {
            this.errorCounterJob = 0;
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
         * Set status STOPPING
         */
        public void setStopping() {
            state = AppState.STOPPING;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounterMqi = 0;
            errorCounterJob = 0;
        }

        /**
         * Set status ERROR
         */
        public void setErrorMqi(final int maxErrorCounter) {
            if (!isStopping()) {
                state = AppState.ERROR;
                dateLastChangeMs = System.currentTimeMillis();
                errorCounterMqi++;
                if (errorCounterMqi >= maxErrorCounter) {
                    setFatalError();
                }
            }
        }
        
        /**
         * Set status ERROR
         */
        public void setErrorJob(final int maxErrorCounter) {
            if (!isStopping()) {
                state = AppState.ERROR;
                dateLastChangeMs = System.currentTimeMillis();
                errorCounterJob++;
                if (errorCounterJob >= maxErrorCounter) {
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
            System.exit(0);
        }
    }
}
