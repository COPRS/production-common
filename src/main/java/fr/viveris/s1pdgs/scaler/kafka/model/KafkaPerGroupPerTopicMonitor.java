package fr.viveris.s1pdgs.scaler.kafka.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
	private final Map<String, Long> lagPerConsumers;

	/**
	 * Lag per partition
	 */
	private final Map<Integer, Long> lagPerPartition;

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
	public KafkaPerGroupPerTopicMonitor(final Date monitoringDate, final String groupId, final String topicName) {
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
	public void setMonitoringDate(final Date monitoringDate) {
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
	public void setGroupId(final String groupId) {
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
	public void setTopicName(final String topicName) {
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
	public void setNbConsumers(final int nbConsumers) {
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
	public void setNbPartitions(final int nbPartitions) {
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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"{monitoringDate: %s, groupId: %s, topicName: %s, nbConsumers: %s, nbPartitions: %s, lagPerConsumers: %s, lagPerPartition: %s}",
				monitoringDate, groupId, topicName, nbConsumers, nbPartitions, lagPerConsumers, lagPerPartition);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(monitoringDate, groupId, topicName, nbConsumers, nbPartitions, lagPerConsumers,
				lagPerPartition);
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
			final KafkaPerGroupPerTopicMonitor other = (KafkaPerGroupPerTopicMonitor) obj;
			ret = Objects.equals(monitoringDate, other.monitoringDate) && Objects.equals(groupId, other.groupId)
					&& Objects.equals(topicName, other.topicName) && nbConsumers == other.nbConsumers
					&& nbPartitions == other.nbPartitions && Objects.equals(lagPerConsumers, other.lagPerConsumers)
					&& Objects.equals(lagPerPartition, other.lagPerPartition);
		}
		return ret;
	}

}
