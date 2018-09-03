package esa.s1pdgs.cpoc.scaler.kafka.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConsumerGroupsDescription {

	/**
	 * Group identifier
	 */
	private String groupId;

	/**
	 * Description of consumers:
	 * <li>key = consumer identifier</li>
	 * <li>value = description</li>
	 */
	private final Map<String, ConsumerDescription> descPerConsumer;

	/**
	 * Description of partitions:
	 * <li>key = consumer identifier</li>
	 * <li>value = description</li>
	 */
	private final Map<String, PartitionDescription> descPerPartition;

	/**
	 * 
	 * @param groupId
	 */
	public ConsumerGroupsDescription(final String groupId) {
		this.groupId = groupId;
		this.descPerConsumer = new HashMap<>();
		this.descPerPartition = new HashMap<>();
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(final String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the descPerConsumer
	 */
	public Map<String, ConsumerDescription> getDescPerConsumer() {
		return descPerConsumer;
	}

	/**
	 * @return the descPerPartition
	 */
	public Map<String, PartitionDescription> getDescPerPartition() {
		return descPerPartition;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{groupId: %s, descPerConsumer: %s, descPerPartition: %s}", groupId, descPerConsumer,
				descPerPartition);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(groupId, descPerConsumer, descPerPartition);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			final ConsumerGroupsDescription other = (ConsumerGroupsDescription) obj;
			ret = Objects.equals(groupId, other.groupId) && Objects.equals(descPerConsumer, other.descPerConsumer)
					&& Objects.equals(descPerPartition, other.descPerPartition);
		}
		return ret;
	}

}
