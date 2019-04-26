package esa.s1pdgs.cpoc.scaler.kafka.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.kafka.model.PartitionDescription;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object PartitionDescription
 * 
 * @author Cyrielle Gailliard
 *
 */
public class PartitionDescriptionTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		PartitionDescription obj = new PartitionDescription(1, "topic-name", "consumer-id", 1, 5, 3);
		assertEquals(1, obj.getId());
		assertEquals("topic-name", obj.getTopicName());
		assertEquals("consumer-id", obj.getConsumerId());
		assertEquals(1, obj.getCurrentOffset());
		assertEquals(5, obj.getLogEndOffset());
		assertEquals(3, obj.getLag());

		obj.setId(15);
		obj.setTopicName("test");
		obj.setConsumerId("tutu");
		obj.setCurrentOffset(14);
		obj.setLogEndOffset(2);
		obj.setLag(29);
		assertEquals(15, obj.getId());
		assertEquals("test", obj.getTopicName());
		assertEquals("tutu", obj.getConsumerId());
		assertEquals(14, obj.getCurrentOffset());
		assertEquals(2, obj.getLogEndOffset());
		assertEquals(29, obj.getLag());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		PartitionDescription obj = new PartitionDescription(1, "topic-name", "consumer-id", 1, 5, 3);
		assertEquals(1, obj.getId());
		assertEquals("topic-name", obj.getTopicName());
		assertEquals("consumer-id", obj.getConsumerId());
		assertEquals(1, obj.getCurrentOffset());
		assertEquals(5, obj.getLogEndOffset());
		assertEquals(3, obj.getLag());

		String str = obj.toString();
		assertTrue(str.contains("id: 1"));
		assertTrue(str.contains("topicName: topic-name"));
		assertTrue(str.contains("consumerId: consumer-id"));
		assertTrue(str.contains("currentOffset: 1"));
		assertTrue(str.contains("logEndOffset: 5"));
		assertTrue(str.contains("lag: 3"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(PartitionDescription.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
