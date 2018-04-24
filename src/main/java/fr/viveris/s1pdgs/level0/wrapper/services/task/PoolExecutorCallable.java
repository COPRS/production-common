package fr.viveris.s1pdgs.level0.wrapper.services.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ProcessTimeoutException;

public class PoolExecutorCallable implements Callable<Boolean> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PoolExecutorCallable.class);

	private static final int WAIT_ACTIVE_MAX_COUNTER = 30;

	private static final int WAIT_ACTIVE_TEMPO_MS = 10000;

	private boolean active = false;

	private final List<PoolProcessor> processors;

	private String prefixMonitorLogs;

	/**
	 * Constructor
	 * 
	 * @param numberOfPoolSize
	 * @param pools
	 */
	public PoolExecutorCallable(JobDto job, String prefixMonitorLogs) {
		this.prefixMonitorLogs = prefixMonitorLogs;
		int counter = 0;
		this.processors = new ArrayList<>(job.getPools().size());
		for (JobPoolDto pool : job.getPools()) {
			counter++;
			this.processors.add(new PoolProcessor(pool, job.getJobOrder(), job.getWorkDirectory(),
					String.format("%s [pool %d]", prefixMonitorLogs, counter)));
		}
	}

	public Boolean call() throws CodedException {
		int counter = 0;
		try {
			// Wait for being active (i.e. wait for download of at least one input)
			while (counter < WAIT_ACTIVE_MAX_COUNTER && !isActive() && !Thread.currentThread().isInterrupted()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Wait for processor executor being active");
				}
				Thread.sleep(WAIT_ACTIVE_TEMPO_MS);
				counter++;
			}
		} catch (InterruptedException ie) {
			throw new InternalErrorException(ie.getMessage(), ie);
		}

		if (Thread.currentThread().isInterrupted()) {
			return false;
		}

		if (!isActive()) {
			throw new ProcessTimeoutException(
					"Process executor not set as active after " + counter * WAIT_ACTIVE_TEMPO_MS + " ms");
		}

		LOGGER.info("{} Start launching processes", this.prefixMonitorLogs);
		for (PoolProcessor poolProcessor : processors) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InternalErrorException("Current thread has been interrupted");
			}
			poolProcessor.process();
		}
		return true;
	}

	public synchronized boolean isActive() {
		return this.active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}
}
