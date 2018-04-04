package fr.viveris.s1pdgs.scaler.kafka.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Consumer monitor for a given group and given topic
 * 
 * @author Cyrielle Gailliard
 *
 */
public class KafkaPerGroupPerTopicMonitor {

	/**
	 * Monitoring date
	 */
	private Date monitoringDate;

	/**
	 * Group identifier
	 */
	private String groupId;

	/**
	 * Topic name
	 */
	private String topicName;

	/**
	 * Number of consumers of the group on the topic
	 */
	private int nbConsumers;

	/**
	 * Number of partitions of the topic
	 */
	private int nbPartitions;

	/**
	 * Lag per consumers = sum of partition lag group by consumers
	 */
	private Map<String, Long> lagPerConsumers;

	/**
	 * Lag per partition
	 */
	private Map<Integer, Long> lagPerPartition;

	/**
	 * Default constructor
	 */
	public KafkaPerGroupPerTopicMonitor() {
		this.lagPerConsumers = new HashMap<String, Long>();
		this.lagPerPartition = new HashMap<Integer, Long>();
	}

	/**
	 * @param groupId
	 * @param topicName
	 * @param nbConsumers
	 * @param nbPartitions
	 */
	public KafkaPerGroupPerTopicMonitor(Date monitoringDate, String groupId, String topicName) {
		this();
		this.monitoringDate = monitoringDate;
		this.groupId = groupId;
		this.topicName = topicName;
	}

	/**
	 * @return the monitoringDate
	 */
	public Date getMonitoringDate() {
		return monitoringDate;
	}

	/**
	 * @param monitoringDate
	 *            the monitoringDate to set
	 */
	public void setMonitoringDate(Date monitoringDate) {
		this.monitoringDate = monitoringDate;
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
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * @param topicName
	 *            the topicName to set
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * @return the nbConsumers
	 */
	public int getNbConsumers() {
		return nbConsumers;
	}

	/**
	 * @param nbConsumers
	 *            the nbConsumers to set
	 */
	public void setNbConsumers(int nbConsumers) {
		this.nbConsumers = nbConsumers;
	}

	/**
	 * @return the nbPartitions
	 */
	public int getNbPartitions() {
		return nbPartitions;
	}

	/**
	 * @param nbPartitions
	 *            the nbPartitions to set
	 */
	public void setNbPartitions(int nbPartitions) {
		this.nbPartitions = nbPartitions;
	}

	/**
	 * @return the lagPerConsumers
	 */
	public Map<String, Long> getLagPerConsumers() {
		return lagPerConsumers;
	}

	/**
	 * @return the lagPerPartition
	 */
	public Map<Integer, Long> getLagPerPartition() {
		return lagPerPartition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((monitoringDate == null) ? 0 : monitoringDate.hashCode());
		result = prime * result + ((topicName == null) ? 0 : topicName.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String lagPerConsumersToString = "{";
		for (String keyConsumer : this.lagPerConsumers.keySet()) {
			lagPerConsumersToString += keyConsumer + ": " + this.lagPerConsumers.get(keyConsumer) + ",";
		}
		String lagPerPartitionsToString = "{";
		for (Integer keyPartition : this.lagPerPartition.keySet()) {
			lagPerPartitionsToString += keyPartition + ": " + this.lagPerPartition.get(keyPartition) + ",";
		}
		return "{monitoringDate: " + monitoringDate + ", groupId: " + groupId + ", topicName: " + topicName
				+ ", nbConsumers: " + nbConsumers + ", nbPartitions: " + nbPartitions + ", lagPerConsumers: "
				+ lagPerConsumersToString + ", lagPerPartition: " + lagPerPartitionsToString + "}";
	}

	/*
	 * (non-Javadoc)
	 * 
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
		KafkaPerGroupPerTopicMonitor other = (KafkaPerGroupPerTopicMonitor) obj;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (monitoringDate == null) {
			if (other.monitoringDate != null)
				return false;
		} else if (!monitoringDate.equals(other.monitoringDate))
			return false;
		if (topicName == null) {
			if (other.topicName != null)
				return false;
		} else if (!topicName.equals(other.topicName))
			return false;
		return true;
	}

}
