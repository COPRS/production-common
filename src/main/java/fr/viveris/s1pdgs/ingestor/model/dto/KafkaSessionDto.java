package fr.viveris.s1pdgs.ingestor.model.dto;

import java.util.Objects;

/**
 * DTO object for publishing in topic "session"
 * 
 * @author Cyrielle Gailliard
 *
 */
public class KafkaSessionDto {

	/**
	 * Session identifier
	 */
	private String sessionIdentifier;

	/**
	 * Product name (here filename)
	 */
	protected String productName;

	/**
	 * Key in object storage
	 */
	protected String keyObjectStorage;

	/**
	 * Channel number
	 */
	private int channel;

	/**
	 * 
	 */
	public KafkaSessionDto(String sessionIdentifier, String productName, String keyObjectStorage, int channel) {
		this.sessionIdentifier = sessionIdentifier;
		this.productName = productName;
		this.keyObjectStorage = keyObjectStorage;
		this.channel = channel;
	}

	/**
	 * @return the sessionIdentifier
	 */
	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	/**
	 * @param sessionIdentifier
	 *            the sessionIdentifier to set
	 */
	public void setSessionIdentifier(String sessionIdentifier) {
		this.sessionIdentifier = sessionIdentifier;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName
	 *            the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return the keyObjectStorage
	 */
	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	/**
	 * @param keyObjectStorage
	 *            the keyObjectStorage to set
	 */
	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * String formatting
	 */
	public String toString() {
		String info = String.format(
				"{ 'sessionIdentifier': %s, 'productName': %s, 'channel': %d, 'keyObjectStorage': %s}",
				sessionIdentifier, productName, channel, keyObjectStorage);
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
		KafkaSessionDto kafkaSessionDto = (KafkaSessionDto) o;
		// field comparison
		return Objects.equals(keyObjectStorage, kafkaSessionDto.getKeyObjectStorage())
				&& Objects.equals(sessionIdentifier, kafkaSessionDto.getSessionIdentifier())
				&& Objects.equals(productName, kafkaSessionDto.getProductName())
				&& Objects.equals(channel, kafkaSessionDto.getChannel());
	}

	@Override
	public int hashCode() {
		return Objects.hash(productName, channel, sessionIdentifier, keyObjectStorage);
	}
}
