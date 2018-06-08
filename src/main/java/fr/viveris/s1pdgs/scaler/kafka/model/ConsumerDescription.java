package fr.viveris.s1pdgs.scaler.kafka.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description of a consumer
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ConsumerDescription {

	/**
	 * Client identifier
	 */
	private String clientId;

	/**
	 * Consumer identifier
	 */
	private String consumerId;

	/**
	 * Sum of all its lags on partitions
	 */
	private long totalLag;

	/**
	 * List of assigned partitions
	 */
	private final List<PartitionDescription> partitions;

	/**
	 * Default constructor
	 */
	public ConsumerDescription() {
		partitions = new ArrayList<>();
	}

	/**
	 * 
	 * @param clientId
	 * @param consumerId
	 */
	public ConsumerDescription(final String clientId, final String consumerId) {
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
	public void setClientId(final String clientId) {
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
	public void setConsumerId(final String consumerId) {
		this.consumerId = consumerId;
	}

	/**
	 * @return the totalLag
	 */
	public long getTotalLag() {
		return totalLag;
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
	public void addPartition(final PartitionDescription partition) {
		if (partition != null) {
			this.partitions.add(partition);
			partition.setConsumerId(this.consumerId);
			totalLag += partition.getLag();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{clientId: %s, consumerId: %s, totalLag: %s, partitions: %s}", clientId, consumerId,
				totalLag, partitions);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(clientId, consumerId, totalLag, partitions);
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
			final ConsumerDescription other = (ConsumerDescription) obj;
			ret = Objects.equals(clientId, other.clientId) && Objects.equals(consumerId, other.consumerId)
					&& totalLag == other.totalLag && Objects.equals(partitions, other.partitions);
		}
		return ret;
	}

}
