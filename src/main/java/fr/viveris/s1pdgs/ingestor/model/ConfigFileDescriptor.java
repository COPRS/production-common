package fr.viveris.s1pdgs.ingestor.model;

import java.util.Objects;

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
	 * True if metadata can be extracted from this file
	 */
	private boolean hasToExtractMetadata;

	/**
	 * True if has to be stored in object storage
	 */
	private boolean hasToBeStored;

	/**
	 * True if a directory
	 */
	private boolean isDirectory;

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
	 * @return the hasToExtractMetadata
	 */
	public boolean isHasToExtractMetadata() {
		return hasToExtractMetadata;
	}

	/**
	 * @param hasToExtractMetadata
	 *            the hasToExtractMetadata to set
	 */
	public void setHasToExtractMetadata(boolean hasToExtractMetadata) {
		this.hasToExtractMetadata = hasToExtractMetadata;
	}

	/**
	 * @return the hasToBeStored
	 */
	public boolean isHasToBeStored() {
		return hasToBeStored;
	}

	/**
	 * @param hasToBeStored
	 *            the hasToBeStored to set
	 */
	public void setHasToBeStored(boolean hasToBeStored) {
		this.hasToBeStored = hasToBeStored;
	}

	/**
	 * @return the isDirectory
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * @param isDirectory
	 *            the isDirectory to set
	 */
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	/**
	 * String formatting
	 */
	public String toString() {
		String info = String.format(
				"{ 'relativePath': %s, 'filename': %s, 'extension': %s, 'productName': %s, 'productClass': %s, 'productType': %s, 'missionId': %s, 'satelliteId': %s, 'hasToExtractMetadata': %s, 'hasToBeStored': %s, 'isDirectory': %s, 'keyObjectStorage': %s}",
				relativePath, filename, extension, productName, productClass, productType, missionId, satelliteId,
				hasToExtractMetadata, hasToBeStored, isDirectory, keyObjectStorage);
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
		ConfigFileDescriptor configFileDescriptor = (ConfigFileDescriptor) o;
		// field comparison
		return Objects.equals(keyObjectStorage, configFileDescriptor.getKeyObjectStorage());
	}

	@Override
	public int hashCode() {
		return Objects.hash(relativePath, filename, extension, productName, productClass, productType, missionId,
				satelliteId, hasToExtractMetadata, hasToBeStored, isDirectory, keyObjectStorage);
	}
}
