package esa.s1pdgs.cpoc.compression.worker.process;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.compression.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public class CompressExecutorCallable implements Callable<Void> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(CompressExecutorCallable.class);
	
	private static final Consumer<String> DEFAULT_OUTPUT_CONSUMER = LOGGER::info;

	private CompressionJob job;

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
	public CompressExecutorCallable(final CompressionJob job, final String prefixLogs, ApplicationProperties properties) {
		this.job = job;
		this.properties = properties;
	}

	/**
	 * Process execution: <br/>
	 * - Wait for being active (see {@link ApplicationProperties} wap fields) <br/>
	 * - For each pool, launch in parallel the tasks executions
	 */
	public Void call() throws AbstractCodedException {
		
		LOGGER.debug("command={}, productName={}, workingDirectory={}",properties.getCommand(), job.getInputKeyObjectStorage(), properties.getWorkingDirectory());
		/*completionSrv.submit(new TaskCallable(properties.getCommand(), job.getProductName(),
				properties.getWorkingDirectory(), reporting));*/
		execute(properties.getCommand(), job.getInputKeyObjectStorage(), job.getOutputKeyObjectStorage(), properties.getWorkingDirectory());

		return null;
	}
	
	public TaskResult execute(final String binaryPath, final String inputPath, final String outputPath,
            final String workDirectory) throws InternalErrorException {
		
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("Compression");
		final Reporting reporting = reportingFactory.newReporting(0);
		
		LOGGER.info("Starting compression task using '{}' with input {} and output {} in {}", binaryPath, inputPath, outputPath, workDirectory);
        reporting.begin(new ReportingMessage("Start Task {}", binaryPath));
        
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
			reporting.error(new ReportingMessage("Interrupted Task {}", binaryPath));
			LOGGER.warn("[task {}] [workDirectory {}]  InterruptedException", binaryPath, workDirectory);
			Thread.currentThread().interrupt();
		} catch (IOException ioe) {
			final InternalErrorException ex = new InternalErrorException("Cannot build the command for the task " + binaryPath, ioe);
			reporting.error(new ReportingMessage("[code {}] {}", ex.getCode().getCode(), ex.getLogMessage()));
			throw ex;
		} catch (ExecutionException e) {
			final InternalErrorException ex =  new InternalErrorException("Error on consuming stdout/stderr of task " + binaryPath, e);
			reporting.error(new ReportingMessage("[code {}] {}", ex.getCode().getCode(), ex.getLogMessage()));
			throw ex;
		} finally {
			if (process != null) {
				process.destroy();
			}
		}        
        reporting.end(new ReportingMessage("End Task {} with exit code {}", binaryPath, r));

        return new TaskResult(binaryPath, r);
    }
}
