package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.BuildTaskTableException;
import fr.viveris.s1pdgs.jobgenerator.exception.MaxNumberTaskTablesReachException;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

public abstract class AbstractJobsDispatcher<T> {

	/**
	 * Job Generator settings
	 */
	protected final JobGeneratorSettings jobGeneratorSettings;

	/**
	 * Available task table processors
	 */
	protected Map<String, AbstractJobsGenerator<T>> generators;

	/**
	 * Scheduler containing the job generation processors
	 */
	protected final ThreadPoolTaskScheduler jobGenerationTaskScheduler;

	/**
	 * Job generator factory
	 */
	protected final JobsGeneratorFactory jobsGeneratorFactory;

	/**
	 * Constructor
	 * 
	 * @param xmlConverter
	 * @param s3Services
	 * @param nbMaxSessions
	 * @param nbMaxTaskTables
	 * @throws BuildTaskTableException
	 * @throws JobDispatcherException
	 */
	public AbstractJobsDispatcher(final JobGeneratorSettings taskTablesSettings,
			final JobsGeneratorFactory jobsGeneratorFactory, final ThreadPoolTaskScheduler jobGenerationTaskScheduler) {
		this.jobsGeneratorFactory = jobsGeneratorFactory;
		this.jobGeneratorSettings = taskTablesSettings;
		this.generators = new HashMap<>(this.jobGeneratorSettings.getMaxnboftasktable());
		this.jobGenerationTaskScheduler = jobGenerationTaskScheduler;
	}

	/**
	 * Initialize the task table processors
	 * 
	 * @throws BuildTaskTableException
	 * @throws JobDispatcherException
	 */
	protected void initTaskTables() throws AbstractCodedException {
		// Retrieve list of XML files in the directory
		File directoryXml = new File(this.jobGeneratorSettings.getDiroftasktables());
		if (directoryXml != null && directoryXml.isDirectory()) {
			File[] taskTableFiles = directoryXml.listFiles(parameter -> parameter.isFile());
			if (taskTableFiles != null) {
				if (taskTableFiles.length > this.jobGeneratorSettings.getMaxnboftasktable()) {
					throw new MaxNumberTaskTablesReachException(String.format(
							"Too much task tables %d", taskTableFiles.length));
				}
				for (File taskTableFile : taskTableFiles) {
					AbstractJobsGenerator<T> jobGenerator = this.createJobGenerator(taskTableFile);
					generators.put(taskTableFile.getName(), jobGenerator);
					this.jobGenerationTaskScheduler.scheduleAtFixedRate(jobGenerator,
							jobGeneratorSettings.getJobgenfixedrate());
				}
			}
		}
	}

	protected abstract AbstractJobsGenerator<T> createJobGenerator(File xmlFile) throws AbstractCodedException;

	/**
	 * Dispatch an EDRS session file. <br/>
	 * If we have only one channel => cache it. <br/>
	 * Else check all raws metadata exist and dispatch the session to the right task
	 * table processor. <br/>
	 * 
	 * @param keyObjectStorage
	 * @param channelId
	 * @return
	 * @throws JobDispatcherException
	 */
	public abstract void dispatch(Job<T> job) throws AbstractCodedException;
}
