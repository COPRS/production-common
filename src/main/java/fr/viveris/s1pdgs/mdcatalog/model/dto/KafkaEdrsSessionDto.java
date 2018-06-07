package fr.viveris.s1pdgs.mdcatalog.model.dto;

import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileType;

/**
 * Exchanged object in the topic t-pdgs-edrs-sessions
 * 
 * @author Cyrielle Gailliard
 *
 */
public class KafkaEdrsSessionDto {

	/**
	 * Object storage key in the bucket of EDRS session files
	 */
	private String objectStorageKey;
	
	private int channelId;
	
	private EdrsSessionFileType productType;

	/**
	 * Default constructor
	 */
	public KafkaEdrsSessionDto() {

	}

	/**
	 * Default constructor
	 */
	public KafkaEdrsSessionDto(String objectStorageKey, int channelId, EdrsSessionFileType productType) {
		this.objectStorageKey = objectStorageKey;
		this.channelId = channelId;
		this.productType = productType;
	}

	/**
	 * @return the objectStorageKey
	 */
	public String getObjectStorageKey() {
		return objectStorageKey;
	}

	/**
	 * @param objectStorageKey
	 *            the objectStorageKey to set
	 */
	public void setObjectStorageKey(String objectStorageKey) {
		this.objectStorageKey = objectStorageKey;
	}

	/**
	 * @return the channelId
	 */
	public int getChannelId() {
		return channelId;
	}

	/**
	 * @param channelId the channelId to set
	 */
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public EdrsSessionFileType getProductType() {
		return productType;
	}

	public void setProductType(EdrsSessionFileType productType) {
		this.productType = productType;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{objectStorageKey: %s, channelId: %s, productType: %s}", objectStorageKey, channelId,
				productType);
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
		KafkaEdrsSessionDto other = (KafkaEdrsSessionDto) obj;
		if (channelId != other.channelId)
			return false;
		if (objectStorageKey == null) {
			if (other.objectStorageKey != null)
				return false;
		} else if (!objectStorageKey.equals(other.objectStorageKey))
			return false;
		if (productType != other.productType)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channelId;
		result = prime * result + ((objectStorageKey == null) ? 0 : objectStorageKey.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		return result;
	}

}
