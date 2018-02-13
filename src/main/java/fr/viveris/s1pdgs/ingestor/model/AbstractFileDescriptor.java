package fr.viveris.s1pdgs.ingestor.model;

/**
 * Abstract class describing a file
 * 
 * @author Cyrielle
 *
 */
public class AbstractFileDescriptor {

	/**
	 * Filename with path from the session path
	 */
	protected String relativePath;

	/**
	 * Filename
	 */
	protected String filename;

	/**
	 * File extension
	 */
	protected FileExtension extension;

	/**
	 * Product name (here filename)
	 */
	protected String productName;

	/**
	 * Mission identifier
	 */
	protected String missionId;

	/**
	 * Satellite identifier
	 */
	protected String satelliteId;

	/**
	 * Key in object storage
	 */
	protected String keyObjectStorage;

	/**
	 * Default constructor
	 */
	public AbstractFileDescriptor() {

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
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
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
	public void setExtension(FileExtension extension) {
		this.extension = extension;
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
	 * @return the missionId
	 */
	public String getMissionId() {
		return missionId;
	}

	/**
	 * @param missionId
	 *            the missionId to set
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
	 * @param satelliteId
	 *            the satelliteId to set
	 */
	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
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

}
