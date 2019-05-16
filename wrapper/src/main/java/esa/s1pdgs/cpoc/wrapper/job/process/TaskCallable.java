package esa.s1pdgs.cpoc.wrapper.job.process;

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

/**
 * Execute one process and wait for its completion
 * 
 * @author Viveris Technologies
 */
public class TaskCallable implements Callable<TaskResult> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(TaskCallable.class);
    
    private static final Consumer<String> DEFAULT_OUTPUT_CONSUMER = LOGGER::info;

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
    
    /**
     * Prefix for REPORT
     */
    private final String prefixReport;
    
    private final Consumer<String> stdOutConsumer;
    private final Consumer<String> stdErrConsumer;

    /**
     * @param binaryPath
     * @param jobOrderPath
     * @param workDirectory
     */
    public TaskCallable(final String binaryPath, final String jobOrderPath,
            final String workDirectory, final String prefixReport) {
    	this(binaryPath, jobOrderPath, workDirectory, prefixReport, DEFAULT_OUTPUT_CONSUMER, DEFAULT_OUTPUT_CONSUMER);
    }
    
    TaskCallable(
    		String binaryPath, 
    		String jobOrderPath, 
    		String workDirectory, 
    		String prefixReport,
			Consumer<String> stdOutConsumer, 
			Consumer<String> stdErrConsumer
	) {
		this.binaryPath = binaryPath;
		this.jobOrderPath = jobOrderPath;
		this.workDirectory = workDirectory;
		this.prefixReport = prefixReport;
		this.stdOutConsumer = stdOutConsumer;
		this.stdErrConsumer = stdErrConsumer;
	}

	/**
     * Execution of the binary
     */
    @Override
    public TaskResult call() throws InternalErrorException {
        LOGGER.info("{} [task {}] [workDirectory {}] [START] Starting call",
                prefixReport, binaryPath, workDirectory);

        int r = -1;

        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
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

		} catch (InterruptedException ie) {
			LOGGER.warn("[task {}] [workDirectory {}]  InterruptedException {}", binaryPath, workDirectory,
					ie.getMessage());
			Thread.currentThread().interrupt();
		} catch (IOException ioe) {
			throw new InternalErrorException("Cannot build the command for the task " + binaryPath, ioe);
		} catch (ExecutionException e) {
			throw new InternalErrorException("Error on consuming stdout/stderr of task " + binaryPath, e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
        LOGGER.info(
                "{} [task {}] [workDirectory {}] [STOP OK] Ending call with exit code {}",
                this.prefixReport, binaryPath, workDirectory, r);

        return new TaskResult(binaryPath, r);
    }

}
