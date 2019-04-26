package esa.s1pdgs.cpoc.mdcatalog.extraction.model;


/**
 * Class describing a configuration file (AUX and MPL)
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ConfigFileDescriptor extends AbstractFileDescriptor {

	/**
	 * Product type:
	 */
	private String productType;

	/**
	 * File class (OPER or TEST)
	 */
	private String productClass;

	/**
	 * Default constructor
	 */
	public ConfigFileDescriptor() {
		super();
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

	/**
	 * @return the productClass
	 */
	public String getProductClass() {
		return productClass;
	}

	/**
	 * @param productClass
	 *            the productClass to set
	 */
	public void setProductClass(String productClass) {
		this.productClass = productClass;
	}


	/**
	 * String formatting
	 */
	public String toString() {
		String info = String.format(
				"{ 'relativePath': %s, 'filename': %s, 'extension': %s, 'productName': %s, 'productClass': %s, 'productType': %s, 'missionId': %s, 'satelliteId': %s, 'keyObjectStorage': %s}",
				relativePath, filename, extension, productName, productClass, productType, missionId, satelliteId, keyObjectStorage);
		return info;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigFileDescriptor other = (ConfigFileDescriptor) obj;
		if (productClass == null) {
			if (other.productClass != null)
				return false;
		} else if (!productClass.equals(other.productClass))
			return false;
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((productClass == null) ? 0 : productClass.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		return result;
	}
}
