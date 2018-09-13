package esa.s1pdgs.cpoc.archives.status;

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
    private final ArchiveStatus status;

    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterSlices;
    
    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterReports;
    
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
            @Value("${status.max-error-counter-slices}") final int maxErrorCounterSlices,
            @Value("${status.max-error-counter-reports}") final int maxErrorCounterReports) {
        this.status = new ArchiveStatus();
        this.shallBeStopped = false;
        this.maxErrorCounterSlices = maxErrorCounterSlices;
        this.maxErrorCounterReports = maxErrorCounterReports;
    }

    /**
     * @return the status
     */
    public synchronized ArchiveStatus getStatus() {
        return status;
    }

    /**
     * Set application as waiting
     */
    public synchronized void setWaiting() {
        if(!this.shallBeStopped) {
            this.status.setWaiting();
        }
    }
    
    /**
     * Set application as processing
     */
    public synchronized void setProcessing(String type) {
        if(!this.shallBeStopped && !this.status.isFatalError()) {
            this.status.setProcessing();
            if("SLICES".equals(type)) {
                this.status.resetErrorCounterSlices();
            } else if("REPORTS".equals(type)) {
                this.status.resetErrorCounterReports();
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
        if("SLICES".equals(type)) {
            this.status.setErrorSlices(maxErrorCounterSlices);
        } else if("REPORTS".equals(type)) {
            this.status.setErrorReports(maxErrorCounterReports);
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
    public class ArchiveStatus {

        /**
         * State
         */
        private AppState state;

        /**
         * Date of the last change of the status (old status != new status)
         */
        private long dateLastChangeMs;

        /**
         * Number of consecutive errors for slices
         */
        private int errorCounterSlices;
        
        /**
         * Number of consecutive errors for reports
         */
        private int errorCounterReports;
        

        /**
         * Constrcutor
         */
        public ArchiveStatus() {
            this.state = AppState.WAITING;
            errorCounterSlices = 0;
            errorCounterReports = 0;
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
        public int getErrorCounterSlices() {
            return errorCounterSlices;
        }
                
        /**
         * Reset the error counter for MQI
         */
        public void resetErrorCounterSlices() {
            this.errorCounterSlices = 0;
        }
        
        /**
         * @return the errorCounter
         */
        public int getErrorCounterReports() {
            return errorCounterReports;
        }
                
        /**
         * Reset the error counter for MQI
         */
        public void resetErrorCounterReports() {
            this.errorCounterReports = 0;
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
            this.resetErrorCounterReports();
            this.resetErrorCounterSlices();
        }

        /**
         * Set status ERROR
         */
        public void setErrorSlices(final int maxErrorCounter) {
            if (!isStopping()) {
                state = AppState.ERROR;
                dateLastChangeMs = System.currentTimeMillis();
                errorCounterSlices++;
                if (errorCounterSlices >= maxErrorCounter) {
                    setFatalError();
                }
            }
        }
        
        /**
         * Set status ERROR
         */
        public void setErrorReports(final int maxErrorCounter) {
            if (!isStopping()) {
                state = AppState.ERROR;
                dateLastChangeMs = System.currentTimeMillis();
                errorCounterReports++;
                if (errorCounterReports >= maxErrorCounter) {
                    setFatalError();
                }
            }
        }
        
        /**
         * Set status PROCESSING
         */
        public void setProcessing() {
            state = AppState.PROCESSING;
            dateLastChangeMs = System.currentTimeMillis();
            
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
         * Indicate if state is PROCESSING
         * 
         * @return
         */
        public boolean isProcessing() {
            return state == AppState.PROCESSING;
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
