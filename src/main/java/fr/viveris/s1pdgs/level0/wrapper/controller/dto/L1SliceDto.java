package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

/**
 * DTO class for L1 slices
 * @author Cyrielle Gailliard
 *
 */
public class L1SliceDto {
	/**
	 * Product name of the L1 slices
	 */
	private String productName;
	
	/**
	 * ObjectkeyStorage of the L1 Slices
	 */
	private String keyObjectStorage;
	
	/**
	 * Family name for L1 Slices
	 */
	private String familyName;
	
	/**
	 * Default constructor
	 */
	public L1SliceDto() {
		
	}

	/**
	 * @param productName
	 * @param keyObjectStorage
	 */
	public L1SliceDto(String productName, String keyObjectStorage, String familyName) {
		this();
		this.productName = productName;
		this.keyObjectStorage = keyObjectStorage;
		this.familyName = familyName;
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
	 * @return the familyName
	 */
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L1SliceDto [productName=" + productName + ", keyObjectStorage=" + keyObjectStorage + ", familyName="
				+ familyName + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((familyName == null) ? 0 : familyName.hashCode());
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
		L1SliceDto other = (L1SliceDto) obj;
		if (familyName == null) {
			if (other.familyName != null)
				return false;
		} else if (!familyName.equals(other.familyName))
			return false;
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
