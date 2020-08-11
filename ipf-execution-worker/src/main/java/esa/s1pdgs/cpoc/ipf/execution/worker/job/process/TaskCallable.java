package esa.s1pdgs.cpoc.ipf.execution.worker.job.process;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;

/**
 * Execute one process and wait for its completion
 * 
 * @author Viveris Technologies
 */
public class TaskCallable implements Callable<TaskResult> {

    /**
     * Logger
     */
    public static final Logger LOGGER =
            LogManager.getLogger(TaskCallable.class);

    /**
     * Absolute path of the binary
     */
    private final String binaryPath;

    /**
     * Absolute path of the job order
     */
    private final String jobOrderPath;

    /**
     * Work directory
     */
    private final String workDirectory;
    
    private final Consumer<String> stdOutConsumer;
    private final Consumer<String> stdErrConsumer;
    
    private final ReportingFactory reportingFactory;

    public TaskCallable(
    		final String binaryPath, 
    		final String jobOrderPath, 
    		final String workDirectory, 
			final Consumer<String> stdOutConsumer, 
			final Consumer<String> stdErrConsumer,
			final ReportingFactory reportingFactory
	) {
		this.binaryPath = binaryPath;
		this.jobOrderPath = jobOrderPath;
		this.workDirectory = workDirectory;
		this.stdOutConsumer = stdOutConsumer;
		this.stdErrConsumer = stdErrConsumer;
		this.reportingFactory = reportingFactory;
	}

	/**
     * Execution of the binary
     */
    @Override
    public TaskResult call() throws InternalErrorException {
    	final Reporting reporting = reportingFactory.newReporting("ProcessingTask");
        reporting.begin(new ReportingMessage("Start Task " + binaryPath));
        
        int r = -1;

        Process process = null;
        try {
            final ProcessBuilder builder = new ProcessBuilder();
            builder.command(binaryPath, jobOrderPath);
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

		} catch (final InterruptedException ie) {
			reporting.error(new ReportingMessage("Interrupted Task {}", binaryPath));
			LOGGER.warn("[task {}] [workDirectory {}]  InterruptedException", binaryPath, workDirectory);
			Thread.currentThread().interrupt();
		} catch (final IOException ioe) {
			final InternalErrorException ex = new InternalErrorException("Cannot build the command for the task " + binaryPath, ioe);
			reporting.error(new ReportingMessage("[code {}] {}", ex.getCode().getCode(), ex.getLogMessage()));
			throw ex;
		} catch (final ExecutionException e) {
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
