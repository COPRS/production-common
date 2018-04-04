package fr.viveris.s1pdgs.scaler.kafka.model;

public class PartitionDescription {
	
	private int id;
	
	private String topicName;
	
	private String consumerId;
	
	private long currentOffset;
	
	private long logEndOffset;
	
	private long lag;
	

	/**
	 * @param id
	 * @param topicName
	 * @param consumerId
	 * @param currentOffset
	 * @param logEndOffset
	 * @param lag
	 */
	public PartitionDescription(int id, String topicName, String consumerId, long currentOffset, long logEndOffset,
			long lag) {
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
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * @param topicName the topicName to set
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * @return the consumerId
	 */
	public String getConsumerId() {
		return consumerId;
	}

	/**
	 * @param consumerId the consumerId to set
	 */
	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	/**
	 * @return the currentOffset
	 */
	public long getCurrentOffset() {
		return currentOffset;
	}

	/**
	 * @param currentOffset the currentOffset to set
	 */
	public void setCurrentOffset(long currentOffset) {
		this.currentOffset = currentOffset;
	}

	/**
	 * @return the logEndOffset
	 */
	public long getLogEndOffset() {
		return logEndOffset;
	}

	/**
	 * @param logEndOffset the logEndOffset to set
	 */
	public void setLogEndOffset(long logEndOffset) {
		this.logEndOffset = logEndOffset;
	}

	/**
	 * @return the lag
	 */
	public long getLag() {
		return lag;
	}

	/**
	 * @param lag the lag to set
	 */
	public void setLag(long lag) {
		this.lag = lag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PartitionDescription [id=" + id + ", topicName=" + topicName + ", consumerId=" + consumerId
				+ ", currentOffset=" + currentOffset + ", logEndOffset=" + logEndOffset + ", lag=" + lag + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((topicName == null) ? 0 : topicName.hashCode());
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
		PartitionDescription other = (PartitionDescription) obj;
		if (id != other.id)
			return false;
		if (topicName == null) {
			if (other.topicName != null)
				return false;
		} else if (!topicName.equals(other.topicName))
			return false;
		return true;
	}

}
