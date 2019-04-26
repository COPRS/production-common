package esa.s1pdgs.cpoc.scaler.kafka.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object KafkaPerGroupPerTopicMonitor
 * @author Cyrielle Gailliard
 *
 */
public class KafkaPerGroupMonitorTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		KafkaPerGroupMonitor obj = new KafkaPerGroupMonitor(new Date(), "group-id");
        assertEquals("group-id", obj.getGroupId());
        assertNotNull(obj.getMonitoringDate());
        assertNotNull(obj.getTopics());
		assertNotNull(obj.getLagPerConsumers());
		assertNotNull(obj.getLagPerPartition());
        assertEquals(0, obj.getTopics().size());
		assertEquals(0, obj.getLagPerConsumers().size());
		assertEquals(0, obj.getLagPerPartition().size());
		

        KafkaPerGroupPerTopicMonitor monitor1 = new KafkaPerGroupPerTopicMonitor();
        monitor1.setGroupId("group-id");
        monitor1.setMonitoringDate(new Date());
        monitor1.setTopicName("topic-name");
        this.addLags(monitor1);
        obj.addMonitor(monitor1);
		assertEquals(2, obj.getNbConsumers());
		assertEquals(3, obj.getNbPartitions());
	}

    /**
     * Test constructors
     */
    @Test
    public void testAddMonitor() {
        KafkaPerGroupMonitor obj = new KafkaPerGroupMonitor(new Date(), "group-id");

        KafkaPerGroupPerTopicMonitor monitor1 = new KafkaPerGroupPerTopicMonitor();
        monitor1.setGroupId("group-id");
        monitor1.setMonitoringDate(new Date());
        monitor1.setTopicName("topic-name");
        this.addLags(monitor1);
        obj.addMonitor(monitor1);
        assertEquals(Arrays.asList("topic-name"), obj.getTopics());
        assertEquals(2, obj.getNbConsumers());
        assertEquals(Long.valueOf(3), obj.getLagPerConsumers().get("client1"));
        assertEquals(Long.valueOf(0), obj.getLagPerConsumers().get("client2"));
        assertEquals(3, obj.getNbPartitions());
        assertEquals(Long.valueOf(1), obj.getLagPerPartition().get(Integer.valueOf(0)));
        assertEquals(Long.valueOf(3), obj.getLagPerPartition().get(Integer.valueOf(1)));
        assertEquals(Long.valueOf(2), obj.getLagPerPartition().get(Integer.valueOf(2)));

        KafkaPerGroupPerTopicMonitor monitor2 = new KafkaPerGroupPerTopicMonitor();
        monitor2.setGroupId("group-id");
        monitor2.setMonitoringDate(new Date());
        monitor2.setTopicName("topic-name2");
        this.addLags2(monitor2);
        obj.addMonitor(monitor2);
        assertEquals(Arrays.asList("topic-name","topic-name2"), obj.getTopics());
        assertEquals(3, obj.getNbConsumers());
        assertEquals(Long.valueOf(3), obj.getLagPerConsumers().get("client1"));
        assertEquals(Long.valueOf(1), obj.getLagPerConsumers().get("client2"));
        assertEquals(Long.valueOf(1), obj.getLagPerConsumers().get("client3"));
        assertEquals(4, obj.getNbPartitions());
        assertEquals(Long.valueOf(1), obj.getLagPerPartition().get(Integer.valueOf(0)));
        assertEquals(Long.valueOf(4), obj.getLagPerPartition().get(Integer.valueOf(1)));
        assertEquals(Long.valueOf(6), obj.getLagPerPartition().get(Integer.valueOf(2)));
        assertEquals(Long.valueOf(0), obj.getLagPerPartition().get(Integer.valueOf(3)));

        KafkaPerGroupPerTopicMonitor monitor3 = new KafkaPerGroupPerTopicMonitor();
        monitor3.setGroupId("group-id");
        monitor3.setMonitoringDate(new Date());
        monitor3.setTopicName("topic-name3");
        this.addLags3(monitor3);
        obj.addMonitor(monitor3);
        assertEquals(Arrays.asList("topic-name","topic-name2","topic-name3"), obj.getTopics());
        assertEquals(3, obj.getNbConsumers());
        assertEquals(Long.valueOf(3), obj.getLagPerConsumers().get("client1"));
        assertEquals(Long.valueOf(1), obj.getLagPerConsumers().get("client2"));
        assertEquals(Long.valueOf(6), obj.getLagPerConsumers().get("client3"));
        assertEquals(4, obj.getNbPartitions());
        assertEquals(Long.valueOf(2), obj.getLagPerPartition().get(Integer.valueOf(0)));
        assertEquals(Long.valueOf(4), obj.getLagPerPartition().get(Integer.valueOf(1)));
        assertEquals(Long.valueOf(6), obj.getLagPerPartition().get(Integer.valueOf(2)));
        assertEquals(Long.valueOf(3), obj.getLagPerPartition().get(Integer.valueOf(3)));
    }
    
    private void addLags(KafkaPerGroupPerTopicMonitor obj) {
        obj.setNbConsumers(2);
        obj.setNbPartitions(3);
        obj.getLagPerConsumers().put("client1", Long.valueOf(3));
        obj.getLagPerConsumers().put("client2", Long.valueOf(0));
        obj.getLagPerPartition().put(Integer.valueOf(0), Long.valueOf(1));
        obj.getLagPerPartition().put(Integer.valueOf(1), Long.valueOf(3));
        obj.getLagPerPartition().put(Integer.valueOf(2), Long.valueOf(2));
    }
    
    private void addLags2(KafkaPerGroupPerTopicMonitor obj) {
        obj.setNbConsumers(3);
        obj.setNbPartitions(4);
        obj.getLagPerConsumers().put("client1", Long.valueOf(0));
        obj.getLagPerConsumers().put("client2", Long.valueOf(1));
        obj.getLagPerConsumers().put("client3", Long.valueOf(1));
        obj.getLagPerPartition().put(Integer.valueOf(0), Long.valueOf(0));
        obj.getLagPerPartition().put(Integer.valueOf(1), Long.valueOf(1));
        obj.getLagPerPartition().put(Integer.valueOf(2), Long.valueOf(4));
        obj.getLagPerPartition().put(Integer.valueOf(3), Long.valueOf(0));
    }
    
    private void addLags3(KafkaPerGroupPerTopicMonitor obj) {
        obj.setNbConsumers(2);
        obj.setNbPartitions(2);
        obj.getLagPerConsumers().put("client2", Long.valueOf(0));
        obj.getLagPerConsumers().put("client3", Long.valueOf(5));
        obj.getLagPerPartition().put(Integer.valueOf(0), Long.valueOf(1));
        obj.getLagPerPartition().put(Integer.valueOf(3), Long.valueOf(3));
    }

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		Date date = new Date();
		KafkaPerGroupMonitor obj = new KafkaPerGroupMonitor(date, "group-id");

        KafkaPerGroupPerTopicMonitor monitor1 = new KafkaPerGroupPerTopicMonitor();
        monitor1.setGroupId("group-id");
        monitor1.setMonitoringDate(new Date());
        monitor1.setTopicName("topic-name");
        this.addLags(monitor1);
        obj.addMonitor(monitor1);

		String str = obj.toString();
		assertTrue(str.contains("monitoringDate: " + date.toString()));
		assertTrue(str.contains("groupId: group-id"));
		assertTrue(str.contains("topics: " + Arrays.asList("topic-name")));
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
		EqualsVerifier.forClass(KafkaPerGroupMonitor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
