package esa.s1pdgs.cpoc.compression.process;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.WrapperProcessTimeoutException;
import esa.s1pdgs.cpoc.compression.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;


/**
 * Executor of all process: - pool one after the other - all tasks of the same
 * pool in parallel
 * 
 * @author Viveris Technologies
 */
public class PoolExecutorCallable implements Callable<Boolean> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(PoolExecutorCallable.class);

    /**
     * Indicate if the working directory is ready and the processes can be
     * launched
     */
    private boolean active;
    
    private final ExecutorService execSrv;

    /**
     * Tasks completion service
     */
    private final CompletionService<TaskResult> completionSrv;

    private CompressionJobDto job;

    /**
     * Application properties
     */
    @Autowired
    private ApplicationProperties properties;

    /**
     * Will create one PoolProcessor per pool
     * 
     * @param properties
     * @param job
     * @param prefixMonitorLogs
     */
    public PoolExecutorCallable(final CompressionJobDto job, final String prefixLogs) {
        this.active = false;
        this.job = job;

        this.execSrv = Executors.newFixedThreadPool(1);
        this.completionSrv = new ExecutorCompletionService<>(execSrv);        
    }

    /**
     * Process execution: <br/>
     * - Wait for being active (see {@link ApplicationProperties} wap fields)
     * <br/>
     * - For each pool, launch in parallel the tasks executions
     */
    public Boolean call() throws AbstractCodedException {
        int counter = 0;
        try {
            // Wait for being active (i.e. wait for download of at least one
            // input)
            while (counter < properties.getWapNbMaxLoop() && !isActive()
                    && !isInterrupted()) {
                Thread.sleep(properties.getWapTempoS() * 1000);
                counter++;
            }
        } catch (InterruptedException ie) {
            throw new InternalErrorException(ie.getMessage(), ie);
        }

        if (!isInterrupted()) {
            LOGGER.debug(
                    "counter {} isActive {} isInterrupted {} getWaitActiveProcessNbMaxLoop {}",
                    counter, isActive(), isInterrupted(),
                    properties.getWapNbMaxLoop());

            if (!isActive()) {
                throw new WrapperProcessTimeoutException(
                        "Process executor not set as active after "
                                + counter * properties.getWapTempoS()
                                + " seconds");
            }
            final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "Processing");
            
            final Reporting reporting = reportingFactory.newReporting(0);
            reporting.reportStart("Start compression");
            
            completionSrv.submit(new TaskCallable(properties.getCommand(),job.getInput().getLocalPath(), properties.getWorkingDirectory(), reporting));
            reporting.reportStop("End compression");
            
            return true;
        }
        return false;

    }

    /**
     * chekc if thread is interrupted
     * 
     * @return
     */
    private boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    /**
     * Check if executor is active or not
     * 
     * @return
     */
    public synchronized boolean isActive() {
        return this.active;
    }

    /**
     * Set the executor as active or not
     * 
     * @param active
     */
    public synchronized void setActive(final boolean active) {
        this.active = active;
    }
}
