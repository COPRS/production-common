package fr.viveris.s1pdgs.level0.wrapper.job.process;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.viveris.s1pdgs.common.errors.InternalErrorException;

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
     * @param binaryPath
     * @param jobOrderPath
     * @param workDirectory
     */
    public TaskCallable(final String binaryPath, final String jobOrderPath,
            final String workDirectory) {
        this.binaryPath = binaryPath;
        this.jobOrderPath = jobOrderPath;
        this.workDirectory = workDirectory;
    }

    /**
     * Execution of the binary
     */
    @Override
    public TaskResult call() throws InternalErrorException {
        LOGGER.info("[task {}] [workDirectory {}] Starting call",
                this.binaryPath, this.workDirectory);

        int r = -1;

        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(binaryPath, jobOrderPath);
            builder.directory(new File(workDirectory));

            process = builder.start();
            StreamGobbler streamGobblerStdout =
                    new StreamGobbler(process.getInputStream(), LOGGER::debug);
            StreamGobbler streamGobblerStderr =
                    new StreamGobbler(process.getErrorStream(), LOGGER::debug);
            Executors.newSingleThreadExecutor().submit(streamGobblerStdout);
            Executors.newSingleThreadExecutor().submit(streamGobblerStderr);
            r = process.waitFor();

        } catch (InterruptedException ie) {
            LOGGER.warn("[task {}] [workDirectory {}]  InterruptedException {}",
                    binaryPath, workDirectory, ie.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException ioe) {
            throw new InternalErrorException(
                    "Cannot build the command for the task " + binaryPath, ioe);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        LOGGER.info(
                "[task {}] [workDirectory {}] Ending call with exit code {}",
                binaryPath, workDirectory, r);

        return new TaskResult(binaryPath, r);
    }

}
