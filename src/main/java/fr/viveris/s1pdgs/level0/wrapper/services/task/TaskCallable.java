package fr.viveris.s1pdgs.level0.wrapper.services.task;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;

public class TaskCallable implements Callable<TaskResult> {

	private static final Logger LOGGER = LogManager.getLogger(TaskCallable.class);

	private final String binaryPath;

	private final String jobOrderPath;

	private final String workDirectory;

	public TaskCallable(String binaryPath, String jobOrderPath, String workDirectory) {
		this.binaryPath = binaryPath;
		this.jobOrderPath = jobOrderPath;
		this.workDirectory = workDirectory;
	}

	@Override
	public TaskResult call() throws InternalErrorException {
		LOGGER.info("[task {}] [workDirectory {}] Starting call", this.binaryPath, this.workDirectory);

		int r = -1;

		Process process = null;
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(this.binaryPath, this.jobOrderPath);
			builder.directory(new File(this.workDirectory));

			process = builder.start();
			StreamGobbler streamGobblerStdout = new StreamGobbler(process.getInputStream(), LOGGER::debug);
			StreamGobbler streamGobblerStderr = new StreamGobbler(process.getErrorStream(), LOGGER::debug);
			Executors.newSingleThreadExecutor().submit(streamGobblerStdout);
			Executors.newSingleThreadExecutor().submit(streamGobblerStderr);
			r = process.waitFor();

		} catch (InterruptedException ie) {
			LOGGER.warn("[task {}] [workDirectory {}]  InterruptedException {}", this.binaryPath, this.workDirectory,
					ie.getMessage());
			Thread.currentThread().interrupt();
		} catch (IOException ioe) {
			throw new InternalErrorException("Cannot build the command for the task " + this.binaryPath, ioe);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		LOGGER.info("[task {}] [workDirectory {}] Ending call with exit code {}", this.binaryPath, this.workDirectory,
				r);

		return new TaskResult(this.binaryPath, r);
	}

}
