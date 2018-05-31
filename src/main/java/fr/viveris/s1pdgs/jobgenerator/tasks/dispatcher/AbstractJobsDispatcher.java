package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.MaxNumberTaskTablesReachException;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

/**
 * 
 * @param <T>
 */
public abstract class AbstractJobsDispatcher<T> {

	/**
	 * Job Generator settings
	 */
	protected final JobGeneratorSettings settings;

	/**
	 * Available task table processors
	 */
	protected Map<String, AbstractJobsGenerator<T>> generators;

	/**
	 * Scheduler containing the job generation processors
	 */
	protected final ThreadPoolTaskScheduler taskScheduler;

	/**
	 * Job generator factory
	 */
	protected final JobsGeneratorFactory factory;

	/**
	 * Constructor
	 * @param settings
	 * @param factory
	 * @param taskScheduler
	 */
	public AbstractJobsDispatcher(final JobGeneratorSettings settings, final JobsGeneratorFactory factory,
			final ThreadPoolTaskScheduler taskScheduler) {
		this.factory = factory;
		this.settings = settings;
		this.generators = new HashMap<>(this.settings.getMaxnboftasktable());
		this.taskScheduler = taskScheduler;
	}

	/**
	 * Initialize the task table processors
	 * 
	 * @throws AbstractCodedException
	 */
	protected void initTaskTables() throws AbstractCodedException {
		// Retrieve list of XML files in the directory
		File directoryXml = new File(this.settings.getDiroftasktables());
		if (directoryXml != null && directoryXml.isDirectory()) {
			File[] taskTableFiles = directoryXml.listFiles(parameter -> parameter.isFile());
			if (taskTableFiles != null) {
				if (taskTableFiles.length > this.settings.getMaxnboftasktable()) {
					throw new MaxNumberTaskTablesReachException(
							String.format("Too much task tables %d", taskTableFiles.length));
				}
				for (File taskTableFile : taskTableFiles) {
					AbstractJobsGenerator<T> jobGenerator = this.createJobGenerator(taskTableFile);
					generators.put(taskTableFile.getName(), jobGenerator);
					this.taskScheduler.scheduleAtFixedRate(jobGenerator, settings.getJobgenfixedrate());
				}
			}
		}
	}

	/**
	 * Create a job generator from the task table XML file
	 * 
	 * @param xmlFile
	 * @return
	 * @throws AbstractCodedException
	 */
	protected abstract AbstractJobsGenerator<T> createJobGenerator(final File xmlFile) throws AbstractCodedException;

	/**
	 * Dispatch an EDRS session file. <br/>
	 * If we have only one channel => cache it. <br/>
	 * Else check all raws metadata exist and dispatch the session to the right task
	 * table processor. <br/>
	 * 
	 * @param job
	 * @throws AbstractCodedException
	 */
	public abstract void dispatch(final Job<T> job) throws AbstractCodedException;
}
