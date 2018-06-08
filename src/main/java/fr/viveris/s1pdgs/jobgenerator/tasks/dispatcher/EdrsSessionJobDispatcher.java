package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

/**
 * Dispatcher of EdrsSession product<br/>
 * Only one task table
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "edrs-sessions")
public class EdrsSessionJobDispatcher extends AbstractJobsDispatcher<EdrsSession> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(EdrsSessionJobDispatcher.class);

	/**
	 * Task table
	 */
	private static final String TASK_TABLE_NAME = "TaskTable.AIOP.xml";

	/**
	 * Constructor
	 * 
	 * @param taskTablesSettings
	 * @param jobsGeneratorFactory
	 * @param jobGenerationTaskScheduler
	 */
	@Autowired
	public EdrsSessionJobDispatcher(final JobGeneratorSettings settings, final JobsGeneratorFactory factory,
			final ThreadPoolTaskScheduler taskScheduler) {
		super(settings, factory, taskScheduler);
	}

	/**
	 * Initialization
	 * 
	 * @throws AbstractCodedException
	 */
	@PostConstruct
	public void initialize() throws AbstractCodedException {
		// Init job generators from task tables
		super.initTaskTables();
	}

	/**
	 * 
	 */
	@Override
	protected AbstractJobsGenerator<EdrsSession> createJobGenerator(final File xmlFile) throws AbstractCodedException {
		return this.factory.createJobGeneratorForEdrsSession(xmlFile);
	}

	/**
	 * 
	 */
	@Override
	public void dispatch(final Job<EdrsSession> job) throws AbstractCodedException {
		LOGGER.info("[MONITOR] [Step 2] [productName {}] [taskTable {}] Caching job", job.getProduct().getIdentifier(),
				TASK_TABLE_NAME);
		this.generators.get(TASK_TABLE_NAME).addJob(job);
	}

}
