package esa.s1pdgs.cpoc.mdcatalog.model.dto;

import java.util.Objects;


/**
 * DTO object for publishing in topic "metadata"
 * @author Cyrielle Gailliard
 *
 */
public class KafkaConfigFileDto {
	/**
	 * Product name of the metadata to index
	 */
	private String productName;
	
	/**
	 * ObjectkeyStorage of the metatdata to index
	 */
	private String keyObjectStorage;
	
	/**
	 * Default constructor
	 */
	public KafkaConfigFileDto() {
	}

	/**
	 * Constructor from all fields
	 * @param action
	 * @param metadata
	 */
	public KafkaConfigFileDto(String productName, String keyObjectStorage) {
		this.productName = productName;
		this.keyObjectStorage = keyObjectStorage;
	}
	

	public String getProductName() {
		return productName;
	}

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
	 * @param keyObjectStorage the keyObjectStorage to set
	 */
	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{productName: %s, keyObjectStorage: %s}", productName, keyObjectStorage);
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
		KafkaConfigFileDto meta = (KafkaConfigFileDto) o;
		// field comparison
		return Objects.equals(productName, meta.getProductName()) && Objects.equals(keyObjectStorage, meta.getKeyObjectStorage());
	}

	@Override
	public int hashCode() {
		return Objects.hash(productName, keyObjectStorage);
	}
}
