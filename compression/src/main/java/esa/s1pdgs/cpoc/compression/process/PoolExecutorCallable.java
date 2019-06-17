package esa.s1pdgs.cpoc.compression.process;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.compression.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public class PoolExecutorCallable implements Callable<Void> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(PoolExecutorCallable.class);
	
	private static final Consumer<String> DEFAULT_OUTPUT_CONSUMER = LOGGER::info;

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
		
		LOGGER.debug("command={}, productName={}, workingDirectory={}",properties.getCommand(), job.getProductName(), properties.getWorkingDirectory());
		/*completionSrv.submit(new TaskCallable(properties.getCommand(), job.getProductName(),
				properties.getWorkingDirectory(), reporting));*/
		execute(properties.getCommand(), job.getProductName(), properties.getWorkingDirectory());

		return null;
	}
	
	public TaskResult execute(final String binaryPath, final String inputPath,
            final String workDirectory) throws InternalErrorException {
		
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "Compression");
		final Reporting reporting = reportingFactory.newReporting(0);
		
		String outputPath = inputPath+".zip";
		LOGGER.info("Starting compression task using '{}' with input {} and output {} in {}", binaryPath, inputPath, outputPath, workDirectory);
        reporting.reportStart("Start Task " + binaryPath);
        
        Consumer<String> stdOutConsumer = DEFAULT_OUTPUT_CONSUMER;
        Consumer<String> stdErrConsumer = DEFAULT_OUTPUT_CONSUMER;
        

        int r = -1;

        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(binaryPath, inputPath, outputPath);
            builder.directory(new File(workDirectory));
            process = builder.start();

            final Future<?> out = Executors.newSingleThreadExecutor().submit(
            		new StreamGobbler(process.getInputStream(), stdOutConsumer)
            );
            final Future<?> err = Executors.newSingleThreadExecutor().submit(
            		new StreamGobbler(process.getErrorStream(), stdErrConsumer)
            );      
            r = process.waitFor();

            // wait for STDOUT/STDERR to be consumed
            out.get();
			err.get();

		} catch (InterruptedException ie) {
			reporting.reportError("Interrupted Task " + binaryPath);
			LOGGER.warn("[task {}] [workDirectory {}]  InterruptedException {}", binaryPath, workDirectory,
					ie.getMessage());
			Thread.currentThread().interrupt();
		} catch (IOException ioe) {
			final InternalErrorException ex = new InternalErrorException("Cannot build the command for the task " + binaryPath, ioe);
			reporting.reportError("[code {}] {}", ex.getCode().getCode(), ex.getLogMessage());
			throw ex;
		} catch (ExecutionException e) {
			final InternalErrorException ex =  new InternalErrorException("Error on consuming stdout/stderr of task " + binaryPath, e);
			reporting.reportError("[code {}] {}", ex.getCode().getCode(), ex.getLogMessage());
			throw ex;
		} finally {
			if (process != null) {
				process.destroy();
			}
		}        
        reporting.reportStop("End Task " + binaryPath + " with exit code " + r);

        return new TaskResult(binaryPath, r);
    }
}
