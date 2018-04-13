package fr.viveris.s1pdgs.scaler.kafka.model;

import java.util.ArrayList;
import java.util.List;

public class ConsumerDescription {

	private String clientId;

	private String consumerId;

	private long totalLag;

	private List<PartitionDescription> partitions;

	public ConsumerDescription() {
		partitions = new ArrayList<>();
	}

	/**
	 * @param clientId
	 * @param consumerId
	 * @param totalLag
	 * @param partitions
	 */
	public ConsumerDescription(String clientId, String consumerId) {
		this();
		this.clientId = clientId;
		this.consumerId = consumerId;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId
	 *            the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
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
	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	/**
	 * @return the totalLag
	 */
	public long getTotalLag() {
		return totalLag;
	}

	/**
	 * @param totalLag
	 *            the totalLag to set
	 */
	public void setTotalLag(long totalLag) {
		this.totalLag = totalLag;
	}

	/**
	 * @return the partitions
	 */
	public List<PartitionDescription> getPartitions() {
		return partitions;
	}

	/**
	 * @param partitions
	 *            the partitions to set
	 */
	public void addPartition(PartitionDescription partition) {
		if (partition != null) {
			this.partitions.add(partition);
			partition.setConsumerId(this.consumerId);
			totalLag += partition.getLag();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConsumerDescription [clientId=" + clientId + ", consumerId=" + consumerId + ", totalLag=" + totalLag
				+ ", partitions=" + partitions + "]";
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
		result = prime * result + ((consumerId == null) ? 0 : consumerId.hashCode());
		return result;
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
		ConsumerDescription other = (ConsumerDescription) obj;
		if (consumerId == null) {
			if (other.consumerId != null)
				return false;
		} else if (!consumerId.equals(other.consumerId))
			return false;
		return true;
	}

}
