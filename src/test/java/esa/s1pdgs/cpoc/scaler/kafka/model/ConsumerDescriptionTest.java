package esa.s1pdgs.cpoc.scaler.kafka.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.kafka.model.ConsumerDescription;
import esa.s1pdgs.cpoc.scaler.kafka.model.PartitionDescription;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object ConsumerDescription
 * @author Cyrielle Gailliard
 *
 */
public class ConsumerDescriptionTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		ConsumerDescription obj = new ConsumerDescription("client-id", "consumer-id");
		assertEquals("client-id", obj.getClientId());
		assertEquals("consumer-id", obj.getConsumerId());
		assertEquals(0, obj.getTotalLag());
		assertNotNull(obj.getPartitions());
		assertTrue(obj.getPartitions().size() == 0);

		PartitionDescription pt1 = new PartitionDescription(1, "topicName", "", 1, 2, 1);
		PartitionDescription pt2 = new PartitionDescription(3, "topicName", "", 0, 0, 0);
		PartitionDescription pt3 = new PartitionDescription(4, "topicName", "", 4, 6, 2);
		obj.addPartition(pt1);
		assertEquals("consumer-id", pt1.getConsumerId());
		assertEquals(1, obj.getTotalLag());
		assertEquals(pt1, obj.getPartitions().get(0));
		obj.addPartition(pt2);
		assertEquals(1, obj.getTotalLag());
		assertEquals(pt2, obj.getPartitions().get(1));
		obj.addPartition(pt3);
		assertEquals(3, obj.getTotalLag());
		assertEquals(pt3, obj.getPartitions().get(2));
		assertEquals(3, obj.getPartitions().size());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		ConsumerDescription obj = new ConsumerDescription();
		obj.setClientId("client-id");
		obj.setConsumerId("consumer-id");

		PartitionDescription pt1 = new PartitionDescription(1, "topicName", "", 1, 2, 1);
		PartitionDescription pt2 = new PartitionDescription(3, "topicName", "", 0, 0, 0);
		PartitionDescription pt3 = new PartitionDescription(4, "topicName", "", 4, 6, 2);
		obj.addPartition(pt1);
		obj.addPartition(pt2);
		obj.addPartition(pt3);

		String str = obj.toString();
		assertTrue(str.contains("clientId: client-id"));
		assertTrue(str.contains("consumerId: consumer-id"));
		assertTrue(str.contains("totalLag: 3"));
		assertTrue(str.contains("partitions: "));
		assertTrue(str.contains(pt2.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(ConsumerDescription.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
