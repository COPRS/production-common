package fr.viveris.s1pdgs.level0.wrapper.services.task;

import java.io.IOException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobTaskDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ProcessExecutionException;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class PoolProcessor {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PoolProcessor.class);

	/**
	 * List of pool
	 */
	private final JobPoolDto pool;

	private final ExecutorService WORKER_THREAD_POOL;

	private final int nbTasks;

	private final String jobOrderPath;

	private final String workDirectory;

	private final CompletionService<TaskResult> service;

	private String prefixMonitorLogs;

	private long timeoutProcessOneTaskS;

	/**
	 * Constructor
	 * 
	 * @param numberOfPoolSize
	 * @param pools
	 */
	public PoolProcessor(JobPoolDto pool, String jobOrderPath, String workDirectory, long timeoutProcessOneTaskS) {
		this.pool = pool;
		this.nbTasks = pool.getTasks().size();
		this.WORKER_THREAD_POOL = Executors.newFixedThreadPool(this.nbTasks);
		this.service = new ExecutorCompletionService<>(WORKER_THREAD_POOL);
		this.jobOrderPath = jobOrderPath;
		this.workDirectory = workDirectory;
		this.prefixMonitorLogs = "[MONITOR] [Step 2]";
		this.timeoutProcessOneTaskS = timeoutProcessOneTaskS;
	}

	public PoolProcessor(JobPoolDto pool, String jobOrderPath, String workDirectory, String prefixMonitorLogs,
			long timeoutProcessOneTask) {
		this(pool, jobOrderPath, workDirectory, timeoutProcessOneTask);
		this.prefixMonitorLogs = prefixMonitorLogs;
	}

	/**
	 * 
	 * @throws ProcessExecutionException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	public void process() throws CodedException {
		boolean stopAllProcessCall = false;
		try {
			LOGGER.info("{} 1 - Submitting tasks {}", this.prefixMonitorLogs, this.pool.getTasks());
			for (JobTaskDto task : pool.getTasks()) {
				this.service.submit(new TaskCallable(task.getBinaryPath(), this.jobOrderPath, this.workDirectory));
			}
			LOGGER.info("{} 2 - Waiting for tasks execution", this.prefixMonitorLogs);
			for (int i = 0; i < this.nbTasks; i++) {
				// Check if interrupted
				if (Thread.currentThread().isInterrupted()) {
					stopAllProcessCall = true;
					throw new InternalErrorException("Pool Processing thread has been interrupted");
				}
				// Wait for a task
				// TODO set in configuration file
				TaskResult r = service.take().get(this.timeoutProcessOneTaskS, TimeUnit.SECONDS);
				int exitCode = r.getExitCode();
				String task = r.getBinary();
				if (exitCode == 0) {
					LOGGER.info("{} 2 - Task {} successfully executed", this.prefixMonitorLogs, task);
				} else if (exitCode >= 0 && exitCode < 128) {
					LOGGER.warn("{} 2 - Task {} exit with warning code {}", this.prefixMonitorLogs, task, exitCode);
				} else {
					this.stopAllTasks();
					throw new ProcessExecutionException(exitCode, "Task " + task + " failed");
				}
			}
		} catch (InterruptedException | TimeoutException e) {
			stopAllProcessCall = true;
			throw new InternalErrorException(e.getMessage(), e);
		} catch (ExecutionException e) {
			stopAllProcessCall = true;
			if (e.getCause().getClass().isAssignableFrom(CodedException.class)) {
				throw (CodedException) e.getCause();
			} else {
				throw new InternalErrorException(e.getMessage(), e);
			}
		} finally {
			if (stopAllProcessCall) {
				try {
					this.stopAllTasks();
				} catch (InterruptedException ie) {
					throw new InternalErrorException(ie.getMessage(), ie);
				}
			}
		}
	}

	private void stopAllTasks() throws InterruptedException {
		this.WORKER_THREAD_POOL.shutdownNow();
		if (!this.WORKER_THREAD_POOL.awaitTermination(this.timeoutProcessOneTaskS, TimeUnit.SECONDS)) {
			// TODO send kill
		}
	}

}