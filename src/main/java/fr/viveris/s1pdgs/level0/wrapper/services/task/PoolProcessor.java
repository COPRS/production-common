package fr.viveris.s1pdgs.level0.wrapper.services.task;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobTaskDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ProcessExecutionException;

/**
 * Launch tasks in parallel of a job pool
 */
public class PoolProcessor {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(PoolProcessor.class);

    /**
     * List of pool
     */
    private final JobPoolDto pool;

    /**
     * Tasks executor service
     */
    private final ExecutorService execSrv;

    /**
     * Tasks completion service
     */
    private final CompletionService<TaskResult> completionSrv;

    /**
     * Number of task in the pool
     */
    private final int nbTasks;

    /**
     * Path of the job order to given in argument of each tasks
     */
    private final String jobOrderPath;

    /**
     * Working directory: directory for launching tasks
     */
    private final String workDirectory;

    /**
     * Prefix for monitor logs
     */
    private final String prefixLogs;

    /**
     * Timeout for processing one tasks. After this time the tasks is considered
     * as failed and all tasks are shutdown
     */
    private final long tmProcessOneTaskS;

    /**
     * @param pool
     * @param jobOrderPath
     * @param workDirectory
     * @param prefixMonitorLogs
     * @param timeoutProcessOneTaskS
     */
    public PoolProcessor(final JobPoolDto pool, final String jobOrderPath,
            final String workDirectory, final String prefixLogs,
            final long tmProcessOneTaskS) {
        this.pool = pool;
        this.nbTasks = pool.getTasks().size();
        this.execSrv = Executors.newFixedThreadPool(this.nbTasks);
        this.completionSrv = new ExecutorCompletionService<>(execSrv);
        this.jobOrderPath = jobOrderPath;
        this.workDirectory = workDirectory;
        this.prefixLogs = prefixLogs;
        this.tmProcessOneTaskS = tmProcessOneTaskS;
    }

    /**
     * Launch all tasks and wait for the completion of each tasks. <br/>
     * W>hen one fails (exit code > 127 or exception raised), all the other
     * running tasks are shutdown and an exception is raised
     * 
     * @throws AbstractCodedException
     */
    public void process() throws AbstractCodedException {
        boolean stopAllProcessCall = false;
        try {
            try {
                LOGGER.info("{} 1 - Submitting tasks {}", prefixLogs,
                        pool.getTasks());
                for (JobTaskDto task : pool.getTasks()) {
                    completionSrv.submit(new TaskCallable(task.getBinaryPath(),
                            jobOrderPath, workDirectory));
                }
                LOGGER.info("{} 2 - Waiting for tasks execution", prefixLogs);
                for (int i = 0; i < nbTasks; i++) {
                    // Check if interrupted
                    if (isInterrupted()) {
                        stopAllProcessCall = true;
                        throw new InternalErrorException(
                                "Pool Processing thread has been interrupted");
                    }
                    // Wait for a task
                    waitNextTaskResult();
                }
            } catch (InterruptedException | TimeoutException e) {
                stopAllProcessCall = true;
                throw new InternalErrorException(e.getMessage(), e);
            } catch (ExecutionException e) {
                stopAllProcessCall = true;
                if (e.getCause() instanceof AbstractCodedException) {
                    throw (AbstractCodedException) e.getCause();
                } else {
                    throw new InternalErrorException(e.getMessage(), e);
                }
            } finally {
                if (stopAllProcessCall) {

                    this.stopAllTasks();

                }
            }
        } catch (InterruptedException ie) {
            throw new InternalErrorException(ie.getMessage(), ie);
        }
    }

    /**
     * Return if thread is interrupted
     * 
     * @return
     */
    private boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    /**
     * Wait for the result of the next completed task:<br/>
     * if code = 0 => OK <br/>
     * if 0 < code <= 127 => warning <br/>
     * if 127 < code => raise exception
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ProcessExecutionException
     */
    private void waitNextTaskResult() throws InterruptedException,
            ExecutionException, TimeoutException, ProcessExecutionException {
        TaskResult r = completionSrv.take().get(this.tmProcessOneTaskS,
                TimeUnit.SECONDS);
        int exitCode = r.getExitCode();
        String task = r.getBinary();
        if (exitCode == 0) {
            LOGGER.info("{} 2 - Task {} successfully executed", this.prefixLogs,
                    task);
        } else if (exitCode >= 0 && exitCode < 128) {
            LOGGER.warn("{} 2 - Task {} exit with warning code {}",
                    this.prefixLogs, task, exitCode);
        } else {
            this.stopAllTasks();
            throw new ProcessExecutionException(exitCode,
                    "Task " + task + " failed");
        }
    }

    /**
     * Stop all tasks
     * @throws InterruptedException
     */
    private void stopAllTasks() throws InterruptedException {
        this.execSrv.shutdownNow();
        if (!this.execSrv.awaitTermination(this.tmProcessOneTaskS,
                TimeUnit.SECONDS)) {
            // TODO send kill
        }
    }

}