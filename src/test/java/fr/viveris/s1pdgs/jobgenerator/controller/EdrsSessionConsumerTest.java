package fr.viveris.s1pdgs.jobgenerator.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.jobgenerator.controller.EdrsSessionsConsumer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.EdrsSessionDto;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFile;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFileRaw;
import fr.viveris.s1pdgs.jobgenerator.model.product.EdrsSessionProduct;
import fr.viveris.s1pdgs.jobgenerator.service.EdrsSessionFileService;
import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.EdrsSessionJobDispatcher;
import fr.viveris.s1pdgs.jobgenerator.utils.TestL0Utils;

/**
 * Test the KAFKA consumer on the topic t-pdgs-edrs-sessions
 * 
 * @author Cyrielle
 *
 */
public class EdrsSessionConsumerTest {

	@Mock
	private EdrsSessionJobDispatcher jobsDispatcher;

	/**
	 * Service for EDRS session file
	 */
	@Mock
	private EdrsSessionFileService edrsSessionFileService;

	/**
	 * Test set up
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {

		// Mcokito
		MockitoAnnotations.initMocks(this);

		// Mock the dispatcher
		Mockito.doAnswer(i -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {

			}
			return true;
		}).when(jobsDispatcher).dispatch(Mockito.any());

		// Mock the dispatcher
		Calendar start1 = Calendar.getInstance();
		start1.set(2017, Calendar.DECEMBER, 11, 14, 22, 37);
		start1.set(Calendar.MILLISECOND, 0);
		Calendar stop1 = Calendar.getInstance();
		stop1.set(2017, Calendar.DECEMBER, 11, 14, 42, 25);
		stop1.set(Calendar.MILLISECOND, 0);
		Mockito.doAnswer(i -> {
			return TestL0Utils.createEdrsSessionFileChannel1(true);
		}).when(edrsSessionFileService).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"), Mockito.eq(1));
		Mockito.doAnswer(i -> {
			return TestL0Utils.createEdrsSessionFileChannel2(true);
		}).when(edrsSessionFileService).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_2"), Mockito.eq(2));
		Mockito.doAnswer(i -> {
			EdrsSessionFile r = new EdrsSessionFile();
			r.setSessionId("SESSION_2");
			r.setStartTime(start1.getTime());
			r.setStopTime(stop1.getTime());
			r.setRawNames(Arrays.asList(new EdrsSessionFileRaw("file1.raw","file1.raw"), new EdrsSessionFileRaw("file2.raw","file2.raw")));
			return r;
		}).when(edrsSessionFileService).createSessionFile(Mockito.eq("KEY_OBS_SESSION_2_1"), Mockito.eq(1));
		Mockito.doAnswer(i -> {
			EdrsSessionFile r = new EdrsSessionFile();
			r.setSessionId("SESSION_2");
			r.setStartTime(start1.getTime());
			r.setStopTime(stop1.getTime());
			r.setRawNames(Arrays.asList(new EdrsSessionFileRaw("file1.raw","file1.raw"), new EdrsSessionFileRaw("file2.raw","file2.raw")));
			return r;
		}).when(edrsSessionFileService).createSessionFile(Mockito.eq("KEY_OBS_SESSION_2_2"), Mockito.eq(2));
	}

	private Map<String, EdrsSessionProduct> getCachedSessions(EdrsSessionsConsumer edrsSessionsConsumer) {
		return edrsSessionsConsumer.cachedSessions;
	}

	/**
	 * Test that KAFKA consumer read a message
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveSession() throws Exception {

		EdrsSessionDto dto1 = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1, "SESSION", "S1", "A");
		EdrsSessionDto dto2 = new EdrsSessionDto("KEY_OBS_SESSION_1_2", 2, "SESSION", "S1", "A");
		EdrsSessionDto dto3 = new EdrsSessionDto("KEY_OBS_SESSION_2_1", 1, "SESSION", "S1", "A");
		EdrsSessionDto dto4 = new EdrsSessionDto("KEY_OBS_SESSION_2_2", 2, "SESSION", "S1", "A");

		EdrsSessionProduct session = TestL0Utils.buildEdrsSessionProduct(true);

		EdrsSessionsConsumer edrsSessionsConsumer = new EdrsSessionsConsumer(jobsDispatcher, edrsSessionFileService,
				10000, 2);

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);
		assertTrue("Cached session map shall be empty", s.isEmpty());

		// Job<EdrsSession> job = new Job<EdrsSession>(session.getSessionId(),
		// session.getStartTime(), session.getStartTime(), session);
		edrsSessionsConsumer.receive(dto1);
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue(s.containsKey(session.getIdentifier()));
		assertEquals(session.getIdentifier(), s.get(session.getIdentifier()).getIdentifier());
		//(session.getStartTime(), s.get(session.getSessionId()).getStartTime());
		//assertEquals(session.getStopTime(), s.get(session.getSessionId()).getStopTime());
		assertEquals(session.getObject().getChannel1().getRawNames().size(), s.get(session.getIdentifier()).getObject().getChannel1().getRawNames().size());
		assertTrue(s.get(session.getIdentifier()).getObject().getChannel2() == null);

		edrsSessionsConsumer.receive(dto3);
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		assertTrue("2 sessions shall be cached", s.size() == 2);
		assertTrue(s.containsKey(session.getIdentifier()));
		assertTrue(s.containsKey("SESSION_2"));
		
		edrsSessionsConsumer.receive(dto2);
		Mockito.verify(jobsDispatcher, times(1)).dispatch(Mockito.any());
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue(s.containsKey("SESSION_2"));
		
		edrsSessionsConsumer.receive(dto4);
		Mockito.verify(jobsDispatcher, times(2)).dispatch(Mockito.any());
		assertTrue("Cached session map shall be empty", s.size() == 0);
	}

	/**
	 * Test that KAFKA consumer read a message
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveRaw() throws Exception {
		EdrsSessionsConsumer edrsSessionsConsumer = new EdrsSessionsConsumer(jobsDispatcher, edrsSessionFileService,
				10000, 2);
		EdrsSessionDto dto1 = new EdrsSessionDto("object storage key", 1, "RAW", "S1", "A");
		edrsSessionsConsumer.receive(dto1);
		Mockito.verify(edrsSessionFileService, never()).createSessionFile(Mockito.anyString(), Mockito.anyInt());
		Mockito.verify(jobsDispatcher, never()).dispatch(Mockito.any());
		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);
		assertTrue("Cached session map shall be empty", s.isEmpty());
	}

	@Test
	public void testMaxSessions() throws Exception {
		EdrsSessionsConsumer edrsSessionsConsumer = new EdrsSessionsConsumer(jobsDispatcher, edrsSessionFileService,
				10000, 1);
		EdrsSessionDto dto1 = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1, "SESSION", "S1", "A");
		EdrsSessionDto dto3 = new EdrsSessionDto("KEY_OBS_SESSION_2_1", 1, "SESSION", "S1", "A");

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);
		assertTrue("Cached session map shall be empty", s.isEmpty());

		edrsSessionsConsumer.receive(dto1);
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, times(1)).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"),
				Mockito.eq(1));
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));

		edrsSessionsConsumer.receive(dto3);
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, times(1)).createSessionFile(Mockito.eq("KEY_OBS_SESSION_2_1"),
				Mockito.eq(1));
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));
	}

	@Test
	public void testReceivedSameMessageTwice() throws Exception {
		EdrsSessionsConsumer edrsSessionsConsumer = new EdrsSessionsConsumer(jobsDispatcher, edrsSessionFileService,
				10000, 2);
		EdrsSessionDto dto1 = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1, "SESSION", "S1", "A");

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);

		edrsSessionsConsumer.receive(dto1);
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, times(1)).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"),
				Mockito.eq(1));
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));

		edrsSessionsConsumer.receive(dto1);
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, times(2)).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"),
				Mockito.eq(1));
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));
	}

	@Test
	public void testCleanCachedSession() throws Exception {
		EdrsSessionDto dto1 = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1, "SESSION", "S1", "A");
		EdrsSessionDto dto3 = new EdrsSessionDto("KEY_OBS_SESSION_2_1", 1, "SESSION", "S1", "A");

		EdrsSessionsConsumer edrsSessionsConsumer = new EdrsSessionsConsumer(jobsDispatcher, edrsSessionFileService,
				10000, 2);

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);
		assertTrue("Cached session map shall be empty", s.isEmpty());

		// Job<EdrsSession> job = new Job<EdrsSession>(session.getSessionId(),
		// session.getStartTime(), session.getStartTime(), session);
		edrsSessionsConsumer.receive(dto1);
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));
		Thread.sleep(11000);
		assertTrue("One session shall be cached", s.size() == 1);
		edrsSessionsConsumer.receive(dto3);
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be SESSION_2", s.containsKey("SESSION_2"));

	}
}
