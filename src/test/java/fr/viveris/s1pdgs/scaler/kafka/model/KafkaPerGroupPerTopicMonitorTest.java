package fr.viveris.s1pdgs.scaler.kafka.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object KafkaPerGroupPerTopicMonitor
 * @author Cyrielle Gailliard
 *
 */
public class KafkaPerGroupPerTopicMonitorTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		KafkaPerGroupPerTopicMonitor obj = new KafkaPerGroupPerTopicMonitor();
		assertNull(obj.getMonitoringDate());
		assertNotNull(obj.getLagPerConsumers());
		assertNotNull(obj.getLagPerPartition());
		assertEquals(0, obj.getLagPerConsumers().size());
		assertEquals(0, obj.getLagPerPartition().size());
		
		obj.setGroupId("group-id");
		obj.setMonitoringDate(new Date());
		obj.setTopicName("topic-name");
		this.addLags(obj);
		assertEquals("group-id", obj.getGroupId());
		assertEquals("topic-name", obj.getTopicName());
		assertNotNull(obj.getMonitoringDate());
		assertEquals(2, obj.getNbConsumers());
		assertEquals(3, obj.getNbPartitions());
		
		Date date = new Date();
		obj = new KafkaPerGroupPerTopicMonitor(date, "group-id-2", "topic-name-2");
		assertNotNull(obj.getMonitoringDate());
		assertEquals("group-id-2", obj.getGroupId());
		assertEquals("topic-name-2", obj.getTopicName());
		assertNotNull(obj.getLagPerConsumers());
		assertNotNull(obj.getLagPerPartition());
		assertEquals(0, obj.getLagPerConsumers().size());
		assertEquals(0, obj.getLagPerPartition().size());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		Date date = new Date();
		KafkaPerGroupPerTopicMonitor obj = new KafkaPerGroupPerTopicMonitor(date, "group-id", "topic-name");
		this.addLags(obj);

		String str = obj.toString();
		assertTrue(str.contains("monitoringDate: " + date.toString()));
		assertTrue(str.contains("groupId: group-id"));
		assertTrue(str.contains("topicName: topic-name"));
		assertTrue(str.contains("nbConsumers: 2"));
		assertTrue(str.contains("nbPartitions: 3"));
		assertTrue(str.contains("lagPerConsumers"));
		assertTrue(str.contains("lagPerPartition"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(KafkaPerGroupPerTopicMonitor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
	
	private void addLags(KafkaPerGroupPerTopicMonitor obj) {
		obj.setNbConsumers(2);
		obj.setNbPartitions(3);
		obj.getLagPerConsumers().put("client1", new Long(3));
		obj.getLagPerConsumers().put("client2", new Long(0));
		obj.getLagPerPartition().put(new Integer(0), new Long(1));
		obj.getLagPerPartition().put(new Integer(1), new Long(0));
		obj.getLagPerPartition().put(new Integer(2), new Long(2));
	}

}
