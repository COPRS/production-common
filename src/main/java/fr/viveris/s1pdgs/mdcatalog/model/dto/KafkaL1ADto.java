package fr.viveris.s1pdgs.mdcatalog.model.dto;

/**
 * DTO Class for L1 Annotaions files
 * @author Olivier Bex-Chauvet
 *
 */
public class KafkaL1ADto {

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
	public KafkaL1ADto() {
		
	}

	/**
	 * Constructor
	 * @param productName
	 * @param keyObjectStorage
	 */
	public KafkaL1ADto(String productName, String keyObjectStorage) {
		super();
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
	 * @param productName the productName to set
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyObjectStorage == null) ? 0 : keyObjectStorage.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
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
		KafkaL1ADto other = (KafkaL1ADto) obj;
		if (keyObjectStorage == null) {
			if (other.keyObjectStorage != null)
				return false;
		} else if (!keyObjectStorage.equals(other.keyObjectStorage))
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		return true;
	}
}
