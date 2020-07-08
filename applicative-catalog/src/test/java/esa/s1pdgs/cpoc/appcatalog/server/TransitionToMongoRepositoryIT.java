package esa.s1pdgs.cpoc.appcatalog.server;

import static esa.s1pdgs.cpoc.common.MessageState.ACK_OK;
import static esa.s1pdgs.cpoc.common.MessageState.ACK_WARN;
import static esa.s1pdgs.cpoc.common.MessageState.READ;
import static esa.s1pdgs.cpoc.common.MessageState.SEND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageDao;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageRepository;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceRepository;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TransitionToMongoRepositoryIT {

	/**
	 * Compares behavior of new JPA Repositories against legacy MongoDB interfaces.
	 * To be used with the mongo-export.tar.gz dataset from https://agile.s1pdgs.eu/browse/S1PRO-1369
	 */
	
	public static final String MQI_MSG_SEQ_KEY = "mqiMessage";
	
	@Autowired
	SequenceRepository seqUut;

	@Autowired
	SequenceDao seqLegacy;
	
	@Autowired
	MqiMessageRepository mqiUut;
	
	@Autowired
	MqiMessageDao mqiLegacy;
	
	@Test
	public void testFindByTopicAndPartitionAndOffsetAndGroup() {
		List<MqiMessage> actual = mqiUut.findByTopicAndPartitionAndOffsetAndGroup("t-pdgs-compression-events", 1, 3068L, "prip-trigger");
		List<MqiMessage> expected = mqiLegacy.searchByTopicPartitionOffsetGroup("t-pdgs-compression-events", 1, 3068L, "prip-trigger");
		assertNotEquals(0L, actual.size());
		assertTrue(actual.equals(expected));
	}
	
	@Test
	public void testFindByTopicAndPartitionAndGroupAndStateNotInOrderByLastReadDateAsc() {
		Set<MessageState> states = new HashSet<>(Arrays.asList(SEND, READ, ACK_OK, ACK_WARN));
		
		List<MqiMessage> actual = mqiUut.findByTopicAndPartitionAndGroupAndStateNotInOrderByLastReadDateAsc(
				"t-pdgs-aio-l0-segment-production-events", 0, "compression-trigger", states);
		
		List<MqiMessage> expected = mqiLegacy.searchByTopicPartitionGroup("t-pdgs-aio-l0-segment-production-events",
				0, "compression-trigger", states);
		
		assertNotEquals(0L, actual.size());
		assertTrue(actual.equals(expected));
	}
	
	@Test
	public void testFindByReadingPodAndCategoryAndStateNotInOrderByCreationDateAsc() {
		Set<MessageState> states = new HashSet<>(Arrays.asList(SEND, READ, ACK_OK, ACK_WARN));

		List<MqiMessage> actual = mqiUut.findByReadingPodAndCategoryAndStateNotInOrderByCreationDateAsc(
				"s1pro-compression-trigger-0", ProductCategory.LEVEL_SEGMENTS, states);
		
		List<MqiMessage> expected = mqiLegacy.searchByPodStateCategory(
				"s1pro-compression-trigger-0", ProductCategory.LEVEL_SEGMENTS, states);
		
		assertNotEquals(0L, actual.size());
		assertTrue(actual.equals(expected));
	}
	
	@Test
	public void testCountByReadingPodAndTopicAndStateIsRead() {
		int actual = mqiUut.countByReadingPodAndTopicAndStateIsRead("s1pro-compression-trigger-0", "t-pdgs-aio-l0-segment-production-events");
		int expected = mqiLegacy.countReadingMessages("s1pro-compression-trigger-0", "t-pdgs-aio-l0-segment-production-events");
		assertEquals(expected, actual);
	}
	
	@Test
	public void testFindById() {
		List<MqiMessage> expected = mqiLegacy.searchByID(83375L);
		Optional<MqiMessage> actual = mqiUut.findById(83375L);
		assertTrue(actual.isPresent());
		assertNotNull(actual.get());
		assertEquals(expected.get(0), actual.get());
	}
	
	@Test
	public void testTruncateBefore() {
		Instant now = Instant.parse("2000-01-01T00:00:00.000Z");
		mqiUut.truncateBefore(Date.from(now));
		
		assertEquals(0, mqiUut.countByReadingPodAndTopicAndStateIsRead("readingPod", "topic"));
		
		
		Date date = Date.from(now.minusMillis(1L));
		MqiMessage msg1 = new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1, 5,
		        "group", MessageState.READ, "readingPod", date,
		        "sendingPod", date, date, 0, date, date);
		msg1.setId(seqLegacy.getNextSequenceId(MQI_MSG_SEQ_KEY));
		mqiUut.insert(msg1);
		
		MqiMessage msg2 = new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1, 5,
		        "group", MessageState.READ, "readingPod", date,
		        "sendingPod", date, date, 0, date, date);
		msg2.setId(seqLegacy.getNextSequenceId(MQI_MSG_SEQ_KEY));
		mqiUut.insert(msg2);

		assertEquals(2, mqiUut.countByReadingPodAndTopicAndStateIsRead("readingPod", "topic"));

		mqiUut.truncateBefore(Date.from(now.minusMillis(1L)));
		
		assertEquals(2, mqiUut.countByReadingPodAndTopicAndStateIsRead("readingPod", "topic"));

		mqiUut.truncateBefore(Date.from(now));
		
		assertEquals(0, mqiUut.countByReadingPodAndTopicAndStateIsRead("readingPod", "topic"));
	}
	
}
