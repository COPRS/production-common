package fr.viveris.s1pdgs.scaler.monitoring.kafka.model;

import java.util.HashMap;
import java.util.Map;

public class ConsumerGroupsDescription {
	
	private String groupId;
	
	private Map<String, ConsumerDescription> descriptionPerConsumer;
	
	private Map<String, PartitionDescription> descriptionPerPartition;

	public ConsumerGroupsDescription(String groupId) {
		this.groupId = groupId;
		this.descriptionPerConsumer = new HashMap<>();
		this.descriptionPerPartition = new HashMap<>();
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the descriptionPerConsumer
	 */
	public Map<String, ConsumerDescription> getDescriptionPerConsumer() {
		return descriptionPerConsumer;
	}

	/**
	 * @return the descriptionPerPartition
	 */
	public Map<String, PartitionDescription> getDescriptionPerPartition() {
		return descriptionPerPartition;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConsumerGroupsDescription [groupId=" + groupId + ", descriptionPerConsumer=" + descriptionPerConsumer
				+ ", descriptionPerPartition=" + descriptionPerPartition + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsumerGroupsDescription other = (ConsumerGroupsDescription) obj;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		return true;
	}

}
