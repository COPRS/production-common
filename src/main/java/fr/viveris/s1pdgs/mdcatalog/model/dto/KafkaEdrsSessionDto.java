package fr.viveris.s1pdgs.mdcatalog.model.dto;

import java.util.Objects;

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
	 * String formatting (JSON format)
	 */
	public String toString() {
		String info = String.format("{'objectStorageKey': %s, 'channelId': %d}", this.objectStorageKey, this.channelId);
		return info;
	}

	@Override
	public boolean equals(Object o) {
		// self check
		if (this == o)
			return true;
		// null check
		if (o == null)
			return false;
		// type check and cast
		if (getClass() != o.getClass())
			return false;
		KafkaEdrsSessionDto dto = (KafkaEdrsSessionDto) o;
		// field comparison
		return Objects.equals(this.objectStorageKey, dto.getObjectStorageKey());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.objectStorageKey);
	}

}
