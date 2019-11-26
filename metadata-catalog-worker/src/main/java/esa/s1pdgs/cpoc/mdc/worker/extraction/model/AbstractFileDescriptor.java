package esa.s1pdgs.cpoc.mdc.worker.extraction.model;

import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Abstract class describing a file
 * 
 * @author Cyrielle
 *
 */
public class AbstractFileDescriptor {

	/**
	 * Product type
	 */
	protected String productType;

	/**
	 * File class
	 */
	protected String productClass;

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
	 * Product Family
	 */
	protected ProductFamily productFamily;

	/**
	 * Mode
	 */
	protected String mode;

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProductClass() {
		return productClass;
	}

	public void setProductClass(String productClass) {
		this.productClass = productClass;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public FileExtension getExtension() {
		return extension;
	}

	public void setExtension(FileExtension extension) {
		this.extension = extension;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	public String getSatelliteId() {
		return satelliteId;
	}

	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
	}

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
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
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((productClass == null) ? 0 : productClass.hashCode());
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + ((missionId == null) ? 0 : missionId.hashCode());
		result = prime * result + ((satelliteId == null) ? 0 : satelliteId.hashCode());
		result = prime * result + ((keyObjectStorage == null) ? 0 : keyObjectStorage.hashCode());
		result = prime * result + ((productFamily == null) ? 0 : productFamily.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
		AbstractFileDescriptor other = (AbstractFileDescriptor) obj;

		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;

		if (productClass == null) {
			if (other.productClass != null)
				return false;
		} else if (!productClass.equals(other.productClass))
			return false;

		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		} else if (!relativePath.equals(other.relativePath))
			return false;

		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;

		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;

		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;

		if (missionId == null) {
			if (other.missionId != null)
				return false;
		} else if (!missionId.equals(other.missionId))
			return false;

		if (satelliteId == null) {
			if (other.satelliteId != null)
				return false;
		} else if (!satelliteId.equals(other.satelliteId))
			return false;

		if (keyObjectStorage == null) {
			if (other.keyObjectStorage != null)
				return false;
		} else if (!keyObjectStorage.equals(other.keyObjectStorage))
			return false;

		if (productFamily == null) {
			if (other.productFamily != null)
				return false;
		} else if (!productFamily.equals(other.productFamily))
			return false;

		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
			return false;

		return true;
	}

	@Override
	public java.lang.String toString() {

		String info = String.format(
				"{ 'productType': %s, 'productClass': %s, 'relativePath': %s, 'filename': %s, 'extension': %s, 'productName': %s, 'missionId': %s, 'satelliteId': %s, 'keyObjectStorage': %s, 'productFamily': %s, 'mode': %s }",
				productType, productClass, relativePath, filename, extension, productName, missionId, satelliteId,
				keyObjectStorage, productFamily, mode);
		return info;
	}
}
