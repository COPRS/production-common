package fr.viveris.s1pdgs.level0.wrapper.services.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.viveris.s1pdgs.level0.wrapper.config.ApplicationProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ProcessTimeoutException;

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
     * Will create one PoolProcessor per pool
     * 
     * @param properties
     * @param job
     * @param prefixMonitorLogs
     */
    public PoolExecutorCallable(final ApplicationProperties properties,
            final JobDto job, final String prefixLogs) {
        this.active = false;
        this.properties = properties;
        this.prefixMonitorLogs = prefixLogs;
        int counter = 0;
        this.processors = new ArrayList<>(job.getPools().size());
        for (JobPoolDto pool : job.getPools()) {
            counter++;
            this.processors.add(new PoolProcessor(pool, job.getJobOrder(),
                    job.getWorkDirectory(),
                    String.format("%s [pool %d]", prefixMonitorLogs, counter),
                    properties.getTmProcOneTaskS()));
        }
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
                throw new ProcessTimeoutException(
                        "Process executor not set as active after "
                                + counter * properties.getWapTempoS()
                                + " seconds");
            }

            LOGGER.info("{} Start launching processes", prefixMonitorLogs);
            for (PoolProcessor poolProcessor : processors) {
                if (isInterrupted()) {
                    throw new InternalErrorException(
                            "Current thread has been interrupted");
                }
                poolProcessor.process();
            }
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
