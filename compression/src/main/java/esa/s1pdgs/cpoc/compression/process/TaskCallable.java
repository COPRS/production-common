package esa.s1pdgs.cpoc.compression.process;

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
    
    private final String inputPath;
    private final String outputPath;

    /**
     * Work directory
     */
    private final String workDirectory;
    
    private final Consumer<String> stdOutConsumer;
    private final Consumer<String> stdErrConsumer;
    
    private final Reporting reporting;

    /**
     * @param binaryPath
     * @param jobOrderPath
     * @param workDirectory
     */
    public TaskCallable(final String binaryPath, final String inputPath,
            final String workDirectory, final Reporting reporting) {
    	this(binaryPath, inputPath, workDirectory, DEFAULT_OUTPUT_CONSUMER, DEFAULT_OUTPUT_CONSUMER, reporting);
    }
    
    TaskCallable(
    		String binaryPath,
    		String inputPath,
    		String workDirectory, 
			Consumer<String> stdOutConsumer, 
			Consumer<String> stdErrConsumer,
			Reporting reporting
	) {
		this.binaryPath = binaryPath;
		this.inputPath = inputPath;
		this.outputPath = inputPath+".zip";
		this.workDirectory = workDirectory;
		this.stdOutConsumer = stdOutConsumer;
		this.stdErrConsumer = stdErrConsumer;
		this.reporting = reporting;
	}

	/**
     * Execution of the binary
     */
    @Override
    public TaskResult call() throws InternalErrorException {
    	LOGGER.info("Starting compression task using '{}' with input {} and output {} in {}", binaryPath, inputPath, outputPath, workDirectory);
        reporting.reportStart("Start Task " + binaryPath);

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
