package fr.viveris.s1pdgs.ingestor.files.model.dto;

import java.util.Objects;

/**
 * DTO object used to transfer auxiliary files in KAFKA topics
 * 
 * @author Cyrielle Gailliard
 *
 */
public class KafkaConfigFileDto {

	/**
	 * ObjectKeyStore of the metadata to index
	 */
	private String productName;

	/**
	 * ObjectkeyStorage of the metatdata to index
	 */
	private String keyObjectStorage;

	/**
	 * Constructor from all fields
	 * 
	 * @param action
	 * @param metadata
	 */
	public KafkaConfigFileDto(final String productName, final String keyObjectStorage) {
		this.productName = productName;
		this.keyObjectStorage = keyObjectStorage;
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
	public void setProductName(final String productName) {
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
	public void setKeyObjectStorage(final String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{productName: %s, keyObjectStorage: %s}", productName, keyObjectStorage);
	}

	/**
	 * @see java.lang.Object#equals()
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			KafkaConfigFileDto other = (KafkaConfigFileDto) obj;
			// field comparison
			ret = Objects.equals(productName, other.productName)
					&& Objects.equals(keyObjectStorage, other.keyObjectStorage);
		}
		return ret;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(productName, keyObjectStorage);
	}
}
