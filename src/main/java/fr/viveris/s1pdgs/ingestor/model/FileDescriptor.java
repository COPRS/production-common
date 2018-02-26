/**
 * 
 */
package fr.viveris.s1pdgs.ingestor.model;

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
	}

	/**
	 * @return the relativePath
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * @param relativePath the relativePath to set
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
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
	 * @return the hasToExtractMetadata
	 */
	public boolean isHasToBePublished() {
		return hasToBePublished;
	}

	/**
	 * @param hasToExtractMetadata the hasToExtractMetadata to set
	 */
	public void setHasToBePublished(boolean hasToBePublished) {
		this.hasToBePublished = hasToBePublished;
	}

	/**
	 * @return the productType
	 */
	public EdrsSessionFileType getProductType() {
		return productType;
	}

	/**
	 * @param productType the productType to set
	 */
	public void setProductType(EdrsSessionFileType productType) {
		this.productType = productType;
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	/**
	 * @return the extension
	 */
	public FileExtension getExtension() {
		return extension;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(FileExtension extension) {
		this.extension = extension;
	}
	

	/**
	 * @return the missionId
	 */
	public String getMissionId() {
		return missionId;
	}

	/**
	 * @param missionId the missionId to set
	 */
	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	/**
	 * @return the satelliteId
	 */
	public String getSatelliteId() {
		return satelliteId;
	}

	/**
	 * @param satelliteId the satelliteId to set
	 */
	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileDescriptor [relativePath=" + relativePath + ", productName=" + productName + ", keyObjectStorage="
				+ keyObjectStorage + ", hasToBePublished=" + hasToBePublished + ", productType=" + productType
				+ ", channel=" + channel + ", extension=" + extension + ", missionId=" + missionId + ", satelliteId="
				+ satelliteId + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channel;
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + (hasToBePublished ? 1231 : 1237);
		result = prime * result + ((keyObjectStorage == null) ? 0 : keyObjectStorage.hashCode());
		result = prime * result + ((missionId == null) ? 0 : missionId.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
		result = prime * result + ((satelliteId == null) ? 0 : satelliteId.hashCode());
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
		FileDescriptor other = (FileDescriptor) obj;
		if (keyObjectStorage == null) {
			if (other.keyObjectStorage != null)
				return false;
		} else if (!keyObjectStorage.equals(other.keyObjectStorage))
			return false;
		return true;
	}

}
