package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.BuildTaskTableException;
import fr.viveris.s1pdgs.jobgenerator.exception.MaxNumberCachedJobsReachException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.product.EdrsSessionProduct;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.EdrsSessionJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

/**
 * Test the class JobDispatcher
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionJobDispatcherTest {

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

	@Mock
	private EdrsSessionJobsGenerator mockGenerator;

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
	 * Construct a dispatcher
	 * 
	 * @return
	 */
	private EdrsSessionJobDispatcher createSessionDispatcher() {
		return new EdrsSessionJobDispatcher(jobGeneratorSettings, jobsGeneratorFactory, jobGenerationTaskScheduler);
	}

	/**
	 * Mock the JobGeneratorSettings
	 */
	private void mockJobGeneratorSettings() {
		// Mock the job generator settings
		doAnswer(i -> {
			return "./test/data/l0_config/task_tables/";
		}).when(jobGeneratorSettings).getDiroftasktables();
		doAnswer(i -> {
			return 4;
		}).when(jobGeneratorSettings).getMaxnboftasktable();
		doAnswer(i -> {
			return 2000;
		}).when(jobGeneratorSettings).getJobgenfixedrate();
	}

	@Test
	public void testCreate() {
		File taskTable1 = new File("./test/data/l0_config/task_tables/TaskTable.AIOP.xml");

		// Mocks
		try {
			doAnswer(i -> {
				return null;
			}).when(jobsGeneratorFactory).createJobGeneratorForEdrsSession(Mockito.eq(taskTable1));
		} catch (BuildTaskTableException e1) {
			fail("Invalid raised exception: " + e1.getMessage());
		}

		// Intitialize
		EdrsSessionJobDispatcher dispatcher = this.createSessionDispatcher();
		try {
			dispatcher.createJobGenerator(taskTable1);
			verify(jobsGeneratorFactory, times(1)).createJobGeneratorForEdrsSession(any());
			verify(jobsGeneratorFactory, times(1)).createJobGeneratorForEdrsSession(eq(taskTable1));
		} catch (AbstractCodedException e) {
			fail("Invalid raised exception: " + e.getMessage());
		}
	}

	/**
	 * Test the initialize function
	 */
	@Test
	public void testInitialize() {
		File taskTable1 = new File("./test/data/l0_config/task_tables/TaskTable.AIOP.xml");

		// Mocks
		this.mockJobGeneratorSettings();
		try {
			doAnswer(i -> {
				return null;
			}).when(jobsGeneratorFactory).createJobGeneratorForEdrsSession(Mockito.eq(taskTable1));
			doAnswer(i -> {
				return null;
			}).when(jobGenerationTaskScheduler).scheduleAtFixedRate(Mockito.any(), Mockito.any());
		} catch (BuildTaskTableException e1) {
			fail("Invalid raised exception: " + e1.getMessage());
		}

		// Intitialize
		EdrsSessionJobDispatcher dispatcher = this.createSessionDispatcher();
		try {
			dispatcher.initialize();
			verify(jobGenerationTaskScheduler, times(1)).scheduleAtFixedRate(any(), anyLong());
			verify(jobGenerationTaskScheduler, times(1)).scheduleAtFixedRate(any(), eq(2000L));
			verify(jobsGeneratorFactory, times(1)).createJobGeneratorForEdrsSession(any());
			verify(jobsGeneratorFactory, times(1)).createJobGeneratorForEdrsSession(eq(taskTable1));

			assertTrue(dispatcher.generators.size() == 1);
			assertTrue(dispatcher.generators.containsKey(taskTable1.getName()));
		} catch (AbstractCodedException e) {
			fail("Invalid raised exception: " + e.getMessage());
		}
	}

	/**
	 * Test dispatch
	 */
	@Test
	public void testDispatch() {
		File taskTable1 = new File("./test/data/l0_config/task_tables/TaskTable.AIOP.xml");
		EdrsSessionProduct p = new EdrsSessionProduct("TEST", "A", "S1A", new Date(), new Date(), new EdrsSession());
		Job<EdrsSession> job1 = new Job<EdrsSession>(p);

		// Mocks
		this.mockJobGeneratorSettings();
		try {
			doAnswer(i -> {
				return mockGenerator;
			}).when(jobsGeneratorFactory).createJobGeneratorForEdrsSession(eq(taskTable1));
			doAnswer(i -> {
				return null;
			}).when(jobGenerationTaskScheduler).scheduleAtFixedRate(any(), any());
			doNothing().when(mockGenerator).addJob(eq(job1));
		} catch (BuildTaskTableException | MaxNumberCachedJobsReachException e1) {
			fail("Invalid raised exception: " + e1.getMessage());
		}

		// Init dispatcher
		EdrsSessionJobDispatcher dispatcher = this.createSessionDispatcher();
		try {
			dispatcher.initTaskTables();
		} catch (AbstractCodedException e) {
			fail("Invalid raised exception: " + e.getMessage());
		}

		// Dispatch
		try {
			dispatcher.dispatch(job1);
			verify(mockGenerator, times(1)).addJob(eq(job1));
		} catch (AbstractCodedException e) {
			fail("Exception raised: " + e.getMessage());
		}
	}

	/**
	 * Test dispatch
	 * 
	 * @throws MaxNumberCachedJobsReachException
	 * @throws JobDispatcherException 
	 */
	@Test(expected = MaxNumberCachedJobsReachException.class)
	public void testDispatchThrow() throws AbstractCodedException {
		File taskTable1 = new File("./test/data/l0_config/task_tables/TaskTable.AIOP.xml");
		EdrsSessionProduct p = new EdrsSessionProduct("TEST", "A", "S1A", new Date(), new Date(), new EdrsSession());
		Job<EdrsSession> job1 = new Job<EdrsSession>(p);

		// Mocks
		this.mockJobGeneratorSettings();
		try {
			doAnswer(i -> {
				return mockGenerator;
			}).when(jobsGeneratorFactory).createJobGeneratorForEdrsSession(eq(taskTable1));
			doAnswer(i -> {
				return null;
			}).when(jobGenerationTaskScheduler).scheduleAtFixedRate(any(), any());
			doThrow(MaxNumberCachedJobsReachException.class).when(mockGenerator).addJob(eq(job1));
		} catch (BuildTaskTableException | MaxNumberCachedJobsReachException e1) {
			fail("Invalid raised exception: " + e1.getMessage());
		}

		// Init dispatcher
		EdrsSessionJobDispatcher dispatcher = this.createSessionDispatcher();
		try {
			dispatcher.initTaskTables();
		} catch (AbstractCodedException e) {
			fail("Invalid raised exception: " + e.getMessage());
		}

		// Dispatch
		dispatcher.dispatch(job1);
	}
}