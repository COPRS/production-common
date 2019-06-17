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
import esa.s1pdgs.cpoc.compression.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public class PoolExecutorCallable implements Callable<Void> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(PoolExecutorCallable.class);

	private final ExecutorService execSrv;

	/**
	 * Tasks completion service
	 */
	private final CompletionService<TaskResult> completionSrv;

	private CompressionJobDto job;

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
	public PoolExecutorCallable(final CompressionJobDto job, final String prefixLogs, ApplicationProperties properties) {
		this.job = job;
		this.properties = properties;

		this.execSrv = Executors.newFixedThreadPool(1);
		this.completionSrv = new ExecutorCompletionService<>(execSrv);
	}

	/**
	 * Process execution: <br/>
	 * - Wait for being active (see {@link ApplicationProperties} wap fields) <br/>
	 * - For each pool, launch in parallel the tasks executions
	 */
	public Void call() throws AbstractCodedException {
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "Compression");

		final Reporting reporting = reportingFactory.newReporting(0);
		reporting.reportStart("Start compression");
		
		LOGGER.debug("command={}, productName={}, workingDirectory={}",properties.getCommand(), job.getProductName(), properties.getWorkingDirectory());
		completionSrv.submit(new TaskCallable(properties.getCommand(), job.getProductName(),
				properties.getWorkingDirectory(), reporting));
		reporting.reportStop("End compression");

		return null;
	}
}
