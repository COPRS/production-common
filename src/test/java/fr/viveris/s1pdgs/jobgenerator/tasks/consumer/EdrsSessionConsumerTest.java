package fr.viveris.s1pdgs.jobgenerator.tasks.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFile;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFileRaw;
import fr.viveris.s1pdgs.jobgenerator.model.product.EdrsSessionProduct;
import fr.viveris.s1pdgs.jobgenerator.service.EdrsSessionFileService;
import fr.viveris.s1pdgs.jobgenerator.status.AppStatus;
import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.EdrsSessionJobDispatcher;
import fr.viveris.s1pdgs.jobgenerator.utils.TestL0Utils;

public class EdrsSessionConsumerTest {

	@Mock
	private EdrsSessionJobDispatcher jobsDispatcher;

	/**
	 * Service for EDRS session file
	 */
	@Mock
	private EdrsSessionFileService edrsSessionFileService;

	@Mock
	private GenericMqiService<EdrsSessionDto> mqiService;
	/**
	 * Application status
	 */
	@Mock
	private AppStatus appStatus;

	private EdrsSessionDto dto1 = new EdrsSessionDto("KEY_OBS_SESSION_1_1", 1, EdrsSessionFileType.SESSION, "S1", "A");
	private EdrsSessionDto dto2 = new EdrsSessionDto("KEY_OBS_SESSION_1_2", 2, EdrsSessionFileType.SESSION, "S1", "A");
	private EdrsSessionDto dto3 = new EdrsSessionDto("KEY_OBS_SESSION_2_1", 1, EdrsSessionFileType.SESSION, "S1", "A");
	private EdrsSessionDto dto4 = new EdrsSessionDto("KEY_OBS_SESSION_2_2", 2, EdrsSessionFileType.SESSION, "S1", "A");
	private GenericMessageDto<EdrsSessionDto> message1 = new GenericMessageDto<EdrsSessionDto>(1, "", dto1);
	private GenericMessageDto<EdrsSessionDto> message2 = new GenericMessageDto<EdrsSessionDto>(2, "", dto2);
	private GenericMessageDto<EdrsSessionDto> message3 = new GenericMessageDto<EdrsSessionDto>(3, "", dto3);
	private GenericMessageDto<EdrsSessionDto> message4 = new GenericMessageDto<EdrsSessionDto>(4, "", dto4);

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
		}).when(edrsSessionFileService).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"));
		Mockito.doAnswer(i -> {
			return TestL0Utils.createEdrsSessionFileChannel2(true);
		}).when(edrsSessionFileService).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_2"));
		Mockito.doAnswer(i -> {
			EdrsSessionFile r = new EdrsSessionFile();
			r.setSessionId("SESSION_2");
			r.setStartTime(start1.getTime());
			r.setStopTime(stop1.getTime());
			r.setRawNames(Arrays.asList(new EdrsSessionFileRaw("file1.raw", "file1.raw"),
					new EdrsSessionFileRaw("file2.raw", "file2.raw")));
			return r;
		}).when(edrsSessionFileService).createSessionFile(Mockito.eq("KEY_OBS_SESSION_2_1"));
		Mockito.doAnswer(i -> {
			EdrsSessionFile r = new EdrsSessionFile();
			r.setSessionId("SESSION_2");
			r.setStartTime(start1.getTime());
			r.setStopTime(stop1.getTime());
			r.setRawNames(Arrays.asList(new EdrsSessionFileRaw("file1.raw", "file1.raw"),
					new EdrsSessionFileRaw("file2.raw", "file2.raw")));
			return r;
		}).when(edrsSessionFileService).createSessionFile(Mockito.eq("KEY_OBS_SESSION_2_2"));

		// Mock the MQI service
		doReturn(message1, message2, message3, message4).when(mqiService).next();
		doReturn(true).when(mqiService).ack(Mockito.any());

		// Mock app status
		doNothing().when(appStatus).setWaiting();
		doNothing().when(appStatus).setProcessing(Mockito.anyLong());
		doNothing().when(appStatus).setError(Mockito.anyString());

	}

	private Map<String, EdrsSessionProduct> getCachedSessions(EdrsSessionConsumer edrsSessionsConsumer) {
		return edrsSessionsConsumer.cachedSessions;
	}

	/**
	 * Test that KAFKA consumer read a message
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveSession() throws Exception {

		doReturn(message1, message3, message2, message4).when(mqiService).next();

		EdrsSessionProduct session = TestL0Utils.buildEdrsSessionProduct(true);

		EdrsSessionConsumer edrsSessionsConsumer = new EdrsSessionConsumer(mqiService, jobsDispatcher,
				edrsSessionFileService, 10000, 2, appStatus);

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);
		assertTrue("Cached session map shall be empty", s.isEmpty());

		// Job<EdrsSession> job = new Job<EdrsSession>(session.getSessionId(),
		// session.getStartTime(), session.getStartTime(), session);
		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue(s.containsKey(session.getIdentifier()));
		assertEquals(session.getIdentifier(), s.get(session.getIdentifier()).getIdentifier());
		// (session.getStartTime(), s.get(session.getSessionId()).getStartTime());
		// assertEquals(session.getStopTime(),
		// s.get(session.getSessionId()).getStopTime());
		assertEquals(session.getObject().getChannel1().getRawNames().size(),
				s.get(session.getIdentifier()).getObject().getChannel1().getRawNames().size());
		assertTrue(s.get(session.getIdentifier()).getObject().getChannel2() == null);
		verify(appStatus, times(1)).setProcessing(Mockito.eq(1L));
		verify(appStatus, times(1)).setWaiting();

		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		assertTrue("2 sessions shall be cached", s.size() == 2);
		assertTrue(s.containsKey(session.getIdentifier()));
		assertTrue(s.containsKey("SESSION_2"));
		verify(appStatus, times(1)).setProcessing(Mockito.eq(3L));
		verify(appStatus, times(2)).setWaiting();

		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, times(1)).dispatch(Mockito.any());
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue(s.containsKey("SESSION_2"));
		verify(appStatus, times(1)).setProcessing(Mockito.eq(2L));
		verify(appStatus, times(3)).setWaiting();

		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, times(2)).dispatch(Mockito.any());
		assertTrue("Cached session map shall be empty", s.size() == 0);
		verify(appStatus, times(1)).setProcessing(Mockito.eq(4L));
		verify(appStatus, times(4)).setWaiting();
	}

	/**
	 * Test that KAFKA consumer read a message
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveRaw() throws Exception {
		EdrsSessionConsumer edrsSessionsConsumer = new EdrsSessionConsumer(mqiService, jobsDispatcher,
				edrsSessionFileService, 10000, 2, appStatus);
		doReturn(new GenericMessageDto<EdrsSessionDto>(1, "",
				new EdrsSessionDto("KEY_OBS_SESSION_2_2", 2, EdrsSessionFileType.RAW, "S1", "A"))).when(mqiService)
						.next();
		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(edrsSessionFileService, never()).createSessionFile(Mockito.anyString());
		Mockito.verify(jobsDispatcher, never()).dispatch(Mockito.any());
		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);
		assertTrue("Cached session map shall be empty", s.isEmpty());
	}

	@Test
	public void testMaxSessions() throws Exception {
		EdrsSessionConsumer edrsSessionsConsumer = new EdrsSessionConsumer(mqiService, jobsDispatcher,
				edrsSessionFileService, 10000, 1, appStatus);
		doReturn(message1, message3).when(mqiService).next();

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);
		assertTrue("Cached session map shall be empty", s.isEmpty());

		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, times(1)).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"));
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));

		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, times(1)).createSessionFile(Mockito.eq("KEY_OBS_SESSION_2_1"));
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));
	}

	@Test
	public void testReceivedSameMessageTwice() throws Exception {
		EdrsSessionConsumer edrsSessionsConsumer = new EdrsSessionConsumer(mqiService, jobsDispatcher,
				edrsSessionFileService, 10000, 2, appStatus);
		doReturn(message1, message1).when(mqiService).next();

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);

		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, times(1)).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"));
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));

		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, times(2)).createSessionFile(Mockito.eq("KEY_OBS_SESSION_1_1"));
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));
	}

	@Test
	public void testReceivedInvalidProductChannel() throws Exception {
		EdrsSessionConsumer edrsSessionsConsumer = new EdrsSessionConsumer(mqiService, jobsDispatcher,
				edrsSessionFileService, 10000, 2, appStatus);
		dto1.setChannelId(3);
		doReturn(message1).when(mqiService).next();

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);

		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		Mockito.verify(edrsSessionFileService, Mockito.never()).createSessionFile(Mockito.any());
		assertTrue("No session should be cached", s.size() == 0);
	}

	@Test
	public void testCleanCachedSession() throws Exception {
		doReturn(message1, message3).when(mqiService).next();

		EdrsSessionConsumer edrsSessionsConsumer = new EdrsSessionConsumer(mqiService, jobsDispatcher,
				edrsSessionFileService, 10000, 2, appStatus);

		Map<String, EdrsSessionProduct> s = this.getCachedSessions(edrsSessionsConsumer);
		assertTrue("Cached session map shall be empty", s.isEmpty());

		// Job<EdrsSession> job = new Job<EdrsSession>(session.getSessionId(),
		// session.getStartTime(), session.getStartTime(), session);
		edrsSessionsConsumer.consumeMessages();
		Mockito.verify(jobsDispatcher, Mockito.never()).dispatch(Mockito.any());
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be L20171109175634707000125", s.containsKey("L20171109175634707000125"));
		Thread.sleep(11000);
		assertTrue("One session shall be cached", s.size() == 1);
		edrsSessionsConsumer.consumeMessages();
		assertTrue("One session shall be cached", s.size() == 1);
		assertTrue("The cached session shall be SESSION_2", s.containsKey("SESSION_2"));

	}

}
