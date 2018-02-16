/**
 * 
 */
package fr.viveris.s1pdgs.ingestor.model;

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
	private boolean hasToExtractMetadata;
	
	/**
	 * Product type: RAW or SESSION here
	 */
	private EdrsSessionFileType productType;

	/**
	 * Session identifier
	 */
	private String sessionIdentifier;

	/**
	 * Channel number
	 */
	private int channel;
	
	/**
	 * File extension
	 */
	private FileExtension extension;

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
	public boolean isHasToExtractMetadata() {
		return hasToExtractMetadata;
	}

	/**
	 * @param hasToExtractMetadata the hasToExtractMetadata to set
	 */
	public void setHasToExtractMetadata(boolean hasToExtractMetadata) {
		this.hasToExtractMetadata = hasToExtractMetadata;
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
	 * @return the sessionIdentifier
	 */
	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	/**
	 * @param sessionIdentifier the sessionIdentifier to set
	 */
	public void setSessionIdentifier(String sessionIdentifier) {
		this.sessionIdentifier = sessionIdentifier;
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
		FileDescriptor sessionFile = (FileDescriptor) o;
		// field comparison
		return Objects.equals(keyObjectStorage, sessionFile.getKeyObjectStorage());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(relativePath, extension, productName, productType, 
				hasToExtractMetadata, keyObjectStorage, sessionIdentifier, channel);
	}

}
