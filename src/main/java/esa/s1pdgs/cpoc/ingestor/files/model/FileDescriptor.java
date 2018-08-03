/**
 * 
 */
package esa.s1pdgs.cpoc.ingestor.files.model;

import java.util.Objects;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class FileDescriptor {

	/**
	 * Filename with path from the session path
	 */
	private String relativePath;

	/**
	 * Product name (here filename)
	 */
	private String productName;

	/**
	 * Key in object storage
	 */
	private String keyObjectStorage;

	/**
	 * True if metadata can be extracted from this file
	 */
	private boolean hasToBePublished;

	/**
	 * Product type: RAW or SESSION here
	 */
	private EdrsSessionFileType productType;

	/**
	 * Channel number
	 */
	private int channel;

	/**
	 * File extension
	 */
	private FileExtension extension;

	/**
	 * Mission identifier
	 */
	private String missionId;

	/**
	 * Satellite identifier
	 */
	private String satelliteId;

	/**
	 * Constructor
	 */
	public FileDescriptor() {
		super();
		channel = -1;
		hasToBePublished = true;
		extension = FileExtension.UNKNOWN;
	}

	/**
	 * @return the relativePath
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * @param relativePath
	 *            the relativePath to set
	 */
	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
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
	 * @return the hasToExtractMetadata
	 */
	public boolean isHasToBePublished() {
		return hasToBePublished;
	}

	/**
	 * @param hasToExtractMetadata
	 *            the hasToExtractMetadata to set
	 */
	public void setHasToBePublished(final boolean hasToBePublished) {
		this.hasToBePublished = hasToBePublished;
	}

	/**
	 * @return the productType
	 */
	public EdrsSessionFileType getProductType() {
		return productType;
	}

	/**
	 * @param productType
	 *            the productType to set
	 */
	public void setProductType(final EdrsSessionFileType productType) {
		this.productType = productType;
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
	public void setChannel(final int channel) {
		this.channel = channel;
	}

	/**
	 * @return the extension
	 */
	public FileExtension getExtension() {
		return extension;
	}

	/**
	 * @param extension
	 *            the extension to set
	 */
	public void setExtension(final FileExtension extension) {
		this.extension = extension;
	}

	/**
	 * @return the missionId
	 */
	public String getMissionId() {
		return missionId;
	}

	/**
	 * @param missionId
	 *            the missionId to set
	 */
	public void setMissionId(final String missionId) {
		this.missionId = missionId;
	}

	/**
	 * @return the satelliteId
	 */
	public String getSatelliteId() {
		return satelliteId;
	}

	/**
	 * @param satelliteId
	 *            the satelliteId to set
	 */
	public void setSatelliteId(final String satelliteId) {
		this.satelliteId = satelliteId;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"{relativePath: %s, productName: %s, keyObjectStorage: %s, hasToBePublished: %s, productType: %s, channel: %s, extension: %s, missionId: %s, satelliteId: %s}",
				relativePath, productName, keyObjectStorage, hasToBePublished, productType, channel, extension,
				missionId, satelliteId);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(relativePath, productName, keyObjectStorage, hasToBePublished, productType, channel,
				extension, missionId, satelliteId);
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
			FileDescriptor other = (FileDescriptor) obj;
			// field comparison
			ret = Objects.equals(relativePath, other.relativePath) && Objects.equals(productName, other.productName)
					&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
					&& hasToBePublished == other.hasToBePublished && Objects.equals(productType, other.productType)
					&& channel == other.channel && Objects.equals(extension, other.extension)
					&& Objects.equals(missionId, other.missionId) && Objects.equals(satelliteId, other.satelliteId);
		}
		return ret;
	}

}
