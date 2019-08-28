package esa.s1pdgs.cpoc.wrapper.job.process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.WrapperProcessTimeoutException;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.wrapper.config.ApplicationProperties;

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

    /**
     * List of processor (one per pool)
     */
    protected final List<PoolProcessor> processors;

    /**
     * Prefix for monitor logs
     */
    private final String prefixMonitorLogs;

    /**
     * Application properties
     */
    private final ApplicationProperties properties;
    /**
     * Application level
     */
    private final ApplicationLevel appLevel;

    /**
     * Will create one PoolProcessor per pool
     * 
     * @param properties
     * @param job
     * @param prefixMonitorLogs
     */
    public PoolExecutorCallable(final ApplicationProperties properties,
            final LevelJobDto job, final String prefixLogs, final ApplicationLevel appLevel) {
        this.active = false;
        this.properties = properties;
        this.prefixMonitorLogs = prefixLogs;
        int counter = 0;
        this.processors = new ArrayList<>(job.getPools().size());
        for (LevelJobPoolDto pool : job.getPools()) {
            counter++;
            this.processors.add(new PoolProcessor(pool, job.getJobOrder(),
                    job.getWorkDirectory(),
                    String.format("%s [poolCounter %d] [s1pdgsTask %sProcessing] ", prefixMonitorLogs, counter, appLevel),
                    properties.getTmProcOneTaskS()));
        }
        this.appLevel = appLevel;
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
            final Reporting.Factory reportingFactory = new LoggerReporting.Factory("Processing");
            
            final Reporting reporting = reportingFactory.newReporting(0);
            reporting.begin("Start " + appLevel + " processing");
                       
            try {
				for (PoolProcessor poolProcessor : processors) {
				    if (isInterrupted()) {
				        throw new InternalErrorException(
				                "Current thread has been interrupted");
				    }
				    poolProcessor.process(reportingFactory);
				}
			} catch (AbstractCodedException e) {
				reporting.error("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
				throw e;
			}
            reporting.end("End " + appLevel + " processing");
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
