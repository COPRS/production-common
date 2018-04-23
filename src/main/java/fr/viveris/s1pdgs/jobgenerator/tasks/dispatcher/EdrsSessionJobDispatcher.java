package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import java.io.File;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.exception.BuildTaskTableException;
import fr.viveris.s1pdgs.jobgenerator.exception.JobDispatcherException;
import fr.viveris.s1pdgs.jobgenerator.exception.JobGenerationException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

@Service
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "edrs-sessions")
public class EdrsSessionJobDispatcher extends AbstractJobsDispatcher<EdrsSession> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EdrsSessionJobDispatcher.class);

	/**
	 * Task table
	 */
	private static final String TASK_TABLE_NAME = "TaskTable.AIOP.xml";

	@Autowired
	public EdrsSessionJobDispatcher(JobGeneratorSettings taskTablesSettings, JobsGeneratorFactory jobsGeneratorFactory,
			ThreadPoolTaskScheduler jobGenerationTaskScheduler) {
		super(taskTablesSettings, jobsGeneratorFactory, jobGenerationTaskScheduler);
	}

	@PostConstruct
	public void initialize() throws BuildTaskTableException, JobDispatcherException {
		// Init job generators from task tables
		super.initTaskTables();
	}

	@Override
	protected AbstractJobsGenerator<EdrsSession> createJobGenerator(File xmlFile) throws BuildTaskTableException {
		return this.jobsGeneratorFactory.createJobGeneratorForEdrsSession(xmlFile);
	}

	@Override
	public void dispatch(Job<EdrsSession> job) throws JobGenerationException, JobDispatcherException {
		LOGGER.info("[MONITOR] [Step 2] [productName {}] [taskTable {}] Caching job", job.getProduct().getIdentifier(),
				TASK_TABLE_NAME);
		this.generators.get(TASK_TABLE_NAME).addJob(job);
	}

}
