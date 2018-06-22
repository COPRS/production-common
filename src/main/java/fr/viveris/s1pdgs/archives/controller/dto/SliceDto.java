package fr.viveris.s1pdgs.archives.controller.dto;

import fr.viveris.s1pdgs.archives.model.ProductFamily;

/**
 * DTO class for L0 slices
 * @author Cyrielle Gailliard
 *
 */
public class SliceDto {
	/**
	 * Product name of the slice
	 */
	private String productName;
	
	/**
	 * ObjectkeyStorage of the slice
	 */
	private String keyObjectStorage;
	
	/**
	 * Family name of the slice (l0 or l1)
	 */
	private ProductFamily family;

	/**
	 * Default constructor
	 */
	public SliceDto() {
		
	}

	/**
	 * @param productName
	 * @param keyObjectStorage
	 */
	public SliceDto(String productName, String keyObjectStorage, ProductFamily family) {
		this();
		this.productName = productName;
		this.keyObjectStorage = keyObjectStorage;
		this.family = family;
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
	 * @return the family
	 */
	public ProductFamily getFamily() {
		return family;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(ProductFamily family) {
		this.family = family;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SliceDto [productName=" + productName + ", keyObjectStorage=" + keyObjectStorage + ", familyName="
				+ family + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((family == null) ? 0 : family.hashCode());
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
		SliceDto other = (SliceDto) obj;
		if (family == null) {
			if (other.family != null)
				return false;
		} else if (!family.equals(other.family))
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
