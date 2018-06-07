package fr.viveris.s1pdgs.scaler.kafka.model;

import java.util.Objects;

/**
 * Description of a partition for a given consumer
 * 
 * @author Cyrielle Gailliard
 *
 */
public class PartitionDescription {

	/**
	 * Identifier
	 */
	private int id;

	/**
	 * Topic name
	 */
	private String topicName;

	/**
	 * Connected consumer identifier if link to a ConsumerDescription
	 */
	private String consumerId;

	/**
	 * Current offset
	 */
	private long currentOffset;

	/**
	 * Last offset published
	 */
	private long logEndOffset;

	/**
	 * Lag = logEndOffset - currentOffset
	 */
	private long lag;

	/**
	 * @param id
	 * @param topicName
	 * @param consumerId
	 * @param currentOffset
	 * @param logEndOffset
	 * @param lag
	 */
	public PartitionDescription(final int id, final String topicName, final String consumerId, final long currentOffset,
			final long logEndOffset, final long lag) {
		super();
		this.id = id;
		this.topicName = topicName;
		this.consumerId = consumerId;
		this.currentOffset = currentOffset;
		this.logEndOffset = logEndOffset;
		this.lag = lag;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final int id) {
		this.id = id;
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
	 * @return the consumerId
	 */
	public String getConsumerId() {
		return consumerId;
	}

	/**
	 * @param consumerId
	 *            the consumerId to set
	 */
	public void setConsumerId(final String consumerId) {
		this.consumerId = consumerId;
	}

	/**
	 * @return the currentOffset
	 */
	public long getCurrentOffset() {
		return currentOffset;
	}

	/**
	 * @param currentOffset
	 *            the currentOffset to set
	 */
	public void setCurrentOffset(final long currentOffset) {
		this.currentOffset = currentOffset;
	}

	/**
	 * @return the logEndOffset
	 */
	public long getLogEndOffset() {
		return logEndOffset;
	}

	/**
	 * @param logEndOffset
	 *            the logEndOffset to set
	 */
	public void setLogEndOffset(final long logEndOffset) {
		this.logEndOffset = logEndOffset;
	}

	/**
	 * @return the lag
	 */
	public long getLag() {
		return lag;
	}

	/**
	 * @param lag
	 *            the lag to set
	 */
	public void setLag(final long lag) {
		this.lag = lag;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{id: %s, topicName: %s, consumerId: %s, currentOffset: %s, logEndOffset: %s, lag: %s}",
				id, topicName, consumerId, currentOffset, logEndOffset, lag);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, topicName, consumerId, currentOffset, logEndOffset, lag);
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
			final PartitionDescription other = (PartitionDescription) obj;
			ret = id == other.id && Objects.equals(topicName, other.topicName)
					&& Objects.equals(consumerId, other.consumerId) && currentOffset == other.currentOffset
					&& logEndOffset == other.logEndOffset && lag == other.lag;
		}
		return ret;
	}

}
