package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.exception.BuildTaskTableException;
import fr.viveris.s1pdgs.jobgenerator.exception.JobDispatcherException;
import fr.viveris.s1pdgs.jobgenerator.exception.JobGenerationException;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

public class AbstractJobDispatcherTest {

	/**
	 * Job generator factory
	 */
	@Mock
	private JobsGeneratorFactory jobsGeneratorFactory;

	/**
	 * Job generator settings
	 */
	@Mock
	private JobGeneratorSettings jobGeneratorSettings;

	/**
	 * Job generator task scheduler
	 */
	@Mock
	private ThreadPoolTaskScheduler jobGenerationTaskScheduler;

	/**
	 * Test set up
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Mock the JobGeneratorSettings
	 * 
	 * @param maxNbTasktable
	 * @param taskTablesDirectory
	 */
	private void mockJobGeneratorSettings(int maxNbTasktable, String taskTablesDirectory) {
		// Mock the job generator settings
		doAnswer(i -> {
			return taskTablesDirectory;
		}).when(jobGeneratorSettings).getDirectoryoftasktables();
		doAnswer(i -> {
			return maxNbTasktable;
		}).when(jobGeneratorSettings).getMaxnumberoftasktables();
		doAnswer(i -> {
			return 2000;
		}).when(jobGeneratorSettings).getScheduledfixedrate();
	}

	/**
	 * Construct a dispatcher
	 * 
	 * @return
	 */
	private AbstractJobsDispatcherImpl createDispatcher() {
		return new AbstractJobsDispatcherImpl(jobGeneratorSettings, jobsGeneratorFactory, jobGenerationTaskScheduler);
	}

	/**
	 * Test the constructor
	 */
	@Test
	public void testConstructor() {
		this.mockJobGeneratorSettings(4, "./l0_config/task_tables/");
		AbstractJobsDispatcherImpl dispatcher = this.createDispatcher();
		assertTrue(dispatcher.generators.isEmpty());
	}

	/**
	 * Test that if maximal number of task table is reached the initialization
	 * failed
	 */
	@Test
	public void testNbMaxTaskTables() {
		this.mockJobGeneratorSettings(1, "./data_test");
		AbstractJobsDispatcherImpl dispatcher = this.createDispatcher();
		try {
			dispatcher.initTaskTables();
			fail("An exception shall be raised");
		} catch (BuildTaskTableException e) {
			fail("Invalid raised exception: " + e.getMessage());
		} catch (JobDispatcherException e) {
			assertTrue(e.getMessage().contains("too much task"));
		}
	}

	/**
	 * Test the initialize function TODO add several tasktable in data test
	 */
	@Test
	public void testInitialize() {

		// Mocks
		this.mockJobGeneratorSettings(4, "./data_test/generic_config/task_tables/");
		doAnswer(i -> {
			return null;
		}).when(jobGenerationTaskScheduler).scheduleAtFixedRate(Mockito.any(), Mockito.any());

		// Intitialize
		AbstractJobsDispatcherImpl dispatcher = this.createDispatcher();
		try {
			dispatcher.initTaskTables();
			verify(jobGenerationTaskScheduler, times(2)).scheduleAtFixedRate(any(), anyLong());
			verify(jobGenerationTaskScheduler, times(2)).scheduleAtFixedRate(any(), eq(2000L));

			assertTrue(dispatcher.generators.size() == 2);
			assertTrue(dispatcher.generators.containsKey("TaskTable.AIOP.xml"));
			assertTrue(dispatcher.generators.containsKey("IW_RAW__0_GRDH_1.xml"));


			assertTrue(dispatcher.getCounter() == 2);
		} catch (BuildTaskTableException | JobDispatcherException e) {
			fail("Invalid raised exception: " + e.getMessage());
		}
	}

}

class AbstractJobsDispatcherImpl extends AbstractJobsDispatcher<String> {

	private int counter;

	public AbstractJobsDispatcherImpl(JobGeneratorSettings taskTablesSettings,
			JobsGeneratorFactory jobsGeneratorFactory, ThreadPoolTaskScheduler jobGenerationTaskScheduler) {
		super(taskTablesSettings, jobsGeneratorFactory, jobGenerationTaskScheduler);
		this.counter = 0;
	}

	@Override
	protected AbstractJobsGenerator<String> createJobGenerator(File xmlFile) throws BuildTaskTableException {
		this.counter++;
		return null;
	}

	@Override
	public void dispatch(Job<String> job) throws JobGenerationException {

	}

	public int getCounter() {
		return this.counter;
	}
}