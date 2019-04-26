package esa.s1pdgs.cpoc.ingestor.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductFamily;

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
    private final IngestorStatus status;

    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterAux;
    
    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterSes;
    
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
            @Value("${status.max-error-counter-aux}") final int maxErrorCounterAux,
            @Value("${status.max-error-counter-ses}") final int maxErrorCounterSes) {
        this.status = new IngestorStatus();
        this.shallBeStopped = false;
        this.maxErrorCounterAux = maxErrorCounterAux;
        this.maxErrorCounterSes = maxErrorCounterSes;
    }

    /**
     * @return the status
     */
    public synchronized IngestorStatus getStatus() {
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
    public synchronized void setProcessing(ProductFamily family) {
        if(!this.shallBeStopped && !this.status.isFatalError()) {
            this.status.setProcessing();
            if(ProductFamily.AUXILIARY_FILE.equals(family)) {
                this.status.resetErrorCounterAux();
            } else if(ProductFamily.EDRS_SESSION.equals(family)) {
                this.status.resetErrorCounterSes();
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
    public synchronized void setError(ProductFamily type) {
        if(ProductFamily.AUXILIARY_FILE.equals(type)) {
            this.status.setErrorAux(maxErrorCounterAux);
        } else if(ProductFamily.EDRS_SESSION.equals(type)) {
            this.status.setErrorSes(maxErrorCounterSes);
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
    public class IngestorStatus {

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
        private int errorCounterAux;
        
        /**
         * Number of consecutive errors for reports
         */
        private int errorCounterSes;
        

        /**
         * Constrcutor
         */
        public IngestorStatus() {
            this.state = AppState.WAITING;
            errorCounterAux = 0;
            errorCounterSes = 0;
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
        public int getErrorCounterAux() {
            return errorCounterAux;
        }
                
        /**
         * Reset the error counter for MQI
         */
        public void resetErrorCounterAux() {
            this.errorCounterAux = 0;
        }
        
        /**
         * @return the errorCounter
         */
        public int getErrorCounterSes() {
            return errorCounterSes;
        }
                
        /**
         * Reset the error counter for MQI
         */
        public void resetErrorCounterSes() {
            this.errorCounterSes = 0;
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
            this.resetErrorCounterSes();
            this.resetErrorCounterAux();
        }

        /**
         * Set status ERROR
         */
        public void setErrorAux(final int maxErrorCounter) {
            if (!isStopping()) {
                state = AppState.ERROR;
                dateLastChangeMs = System.currentTimeMillis();
                errorCounterAux++;
                if (errorCounterAux >= maxErrorCounter) {
                    setFatalError();
                }
            }
        }
        
        /**
         * Set status ERROR
         */
        public void setErrorSes(final int maxErrorCounter) {
            if (!isStopping()) {
                state = AppState.ERROR;
                dateLastChangeMs = System.currentTimeMillis();
                errorCounterSes++;
                if (errorCounterSes >= maxErrorCounter) {
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
