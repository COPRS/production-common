package esa.s1pdgs.cpoc.scaler.kafka.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.kafka.model.ConsumerDescription;
import esa.s1pdgs.cpoc.scaler.kafka.model.ConsumerGroupsDescription;
import esa.s1pdgs.cpoc.scaler.kafka.model.PartitionDescription;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object ConsumerGroupsDescription
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ConsumerGroupsDescriptionTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		ConsumerGroupsDescription obj = new ConsumerGroupsDescription("group-id");
		assertEquals("group-id", obj.getGroupId());
		assertTrue(0 == obj.getDescPerConsumer().size());
		assertTrue(0 == obj.getDescPerPartition().size());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		ConsumerGroupsDescription obj = new ConsumerGroupsDescription("group-id");
		assertEquals("group-id", obj.getGroupId());
		assertTrue(0 == obj.getDescPerConsumer().size());
		assertTrue(0 == obj.getDescPerPartition().size());

		ConsumerDescription consumer1 = new ConsumerDescription("client-id1", "consumer-id1");
		ConsumerDescription consumer2 = new ConsumerDescription("client-id2", "consumer-id2");

		PartitionDescription pt1 = new PartitionDescription(0, "topic-name", "consumer-id1", 1, 5, 3);
		PartitionDescription pt2 = new PartitionDescription(1, "topic-name", "consumer-id2", 1, 5, 3);
		PartitionDescription pt3 = new PartitionDescription(2, "topic-name", "consumer-id1", 1, 5, 3);

		consumer1.addPartition(pt1);
		consumer1.addPartition(pt3);
		consumer2.addPartition(pt2);

		obj.getDescPerConsumer().put("client-id1", consumer1);
		obj.getDescPerConsumer().put("client-id2", consumer2);

		obj.getDescPerPartition().put("0", pt1);
		obj.getDescPerPartition().put("1", pt2);
		obj.getDescPerPartition().put("2", pt3);

		String str = obj.toString();
		assertTrue(str.contains("groupId: group-id"));
		assertTrue(str.contains("descPerConsumer: "));
		assertTrue(str.contains("descPerPartition: "));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(ConsumerGroupsDescription.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}

}
