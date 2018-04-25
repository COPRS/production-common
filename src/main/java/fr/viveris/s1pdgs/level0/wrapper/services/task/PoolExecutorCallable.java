package fr.viveris.s1pdgs.level0.wrapper.services.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.viveris.s1pdgs.level0.wrapper.config.ApplicationProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ProcessTimeoutException;

public class PoolExecutorCallable implements Callable<Boolean> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(PoolExecutorCallable.class);

	private boolean active = false;

	private final List<PoolProcessor> processors;

	private String prefixMonitorLogs;

	private final ApplicationProperties properties;

	/**
	 * Constructor
	 * 
	 * @param numberOfPoolSize
	 * @param pools
	 */
	public PoolExecutorCallable(final ApplicationProperties properties, JobDto job, String prefixMonitorLogs) {
		this.properties = properties;
		this.prefixMonitorLogs = prefixMonitorLogs;
		int counter = 0;
		this.processors = new ArrayList<>(job.getPools().size());
		for (JobPoolDto pool : job.getPools()) {
			counter++;
			this.processors.add(new PoolProcessor(pool, job.getJobOrder(), job.getWorkDirectory(),
					String.format("%s [pool %d]", prefixMonitorLogs, counter),
					this.properties.getTimeoutProcessOneTaskS()));
		}
	}

	public Boolean call() throws CodedException {
		int counter = 0;
		try {
			// Wait for being active (i.e. wait for download of at least one input)
			while (counter < properties.getWaitActiveProcessNbMaxLoop() && !isActive()
					&& !Thread.currentThread().isInterrupted()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Wait for processor executor being active");
				}
				Thread.sleep(properties.getWaitActiveProcessTempoS() * 1000);
				counter++;
			}
		} catch (InterruptedException ie) {
			throw new InternalErrorException(ie.getMessage(), ie);
		}

		if (Thread.currentThread().isInterrupted()) {
			return false;
		}

		LOGGER.debug("counter {} isActive {} isInterrupted {} getWaitActiveProcessNbMaxLoop {}", counter, isActive(),
				Thread.currentThread().isInterrupted(), properties.getWaitActiveProcessNbMaxLoop());

		if (!isActive()) {
			throw new ProcessTimeoutException("Process executor not set as active after "
					+ counter * properties.getWaitActiveProcessTempoS() + " seconds");
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
