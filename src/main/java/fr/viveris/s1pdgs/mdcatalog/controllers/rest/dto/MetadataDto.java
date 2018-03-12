package fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public abstract class MetadataDto {

	/**
	 * Product name
	 */
	protected String productName;

	/**
	 * Product type
	 */
	protected String productType;

	/**
	 * Key in object storage
	 */
	protected String keyObjectStorage;

	/**
	 * Validity start time
	 */
	protected String validityStart;

	/**
	 * Validity stop time
	 */
	protected String validityStop;

	/**
	 * Constrcutor using fields
	 * 
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param validityStart
	 * @param validityStop
	 */
	public MetadataDto(String productName, String productType, String keyObjectStorage, String validityStart,
			String validityStop) {
		super();
		this.productName = productName;
		this.productType = productType;
		this.keyObjectStorage = keyObjectStorage;
		this.validityStart = validityStart;
		this.validityStop = validityStop;
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public MetadataDto(MetadataDto obj) {
		this(obj.getProductName(), obj.getProductType(), obj.getKeyObjectStorage(), obj.getValidityStart(),
				obj.getValidityStop());
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
	 * @return the validityStart
	 */
	public String getValidityStart() {
		return validityStart;
	}

	/**
	 * @param validityStart
	 *            the validityStart to set
	 */
	public void setValidityStart(String validityStart) {
		this.validityStart = validityStart;
	}

	/**
	 * @return the validityStop
	 */
	public String getValidityStop() {
		return validityStop;
	}

	/**
	 * @param validityStop
	 *            the validityStop to set
	 */
	public void setValidityStop(String validityStop) {
		this.validityStop = validityStop;
	}

	/**
	 * @return the productType
	 */
	public String getProductType() {
		return productType;
	}

	/**
	 * @param productType
	 *            the productType to set
	 */
	public void setProductType(String productType) {
		this.productType = productType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SearchMetadataFile [productName=" + productName + ", productType=" + productType + ", keyObjectStorage="
				+ keyObjectStorage + ", validityStart=" + validityStart + ", validityStop=" + validityStop + "]";
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
		result = prime * result + ((keyObjectStorage == null) ? 0 : keyObjectStorage.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((validityStart == null) ? 0 : validityStart.hashCode());
		result = prime * result + ((validityStop == null) ? 0 : validityStop.hashCode());
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
		MetadataDto other = (MetadataDto) obj;
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
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		if (validityStart == null) {
			if (other.validityStart != null)
				return false;
		} else if (!validityStart.equals(other.validityStart))
			return false;
		if (validityStop == null) {
			if (other.validityStop != null)
				return false;
		} else if (!validityStop.equals(other.validityStop))
			return false;
		return true;
	}

}
