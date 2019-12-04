package esa.s1pdgs.cpoc.mdc.worker.extraction.model;

import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Abstract class describing a file
 * 
 * @author Cyrielle
 *
 */
public abstract class AbstractFileDescriptor {

	protected String productType;
	protected String productClass;
	protected String relativePath;
	protected String filename;
	protected FileExtension extension;
	protected String productName;
	protected String missionId;
	protected String satelliteId;
	protected String keyObjectStorage;
	protected ProductFamily productFamily;
	protected String mode;

	public String getProductType() {
		return productType;
	}

	public void setProductType(final String productType) {
		this.productType = productType;
	}

	public String getProductClass() {
		return productClass;
	}

	public void setProductClass(final String productClass) {
		this.productClass = productClass;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public FileExtension getExtension() {
		return extension;
	}

	public void setExtension(final FileExtension extension) {
		this.extension = extension;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(final String missionId) {
		this.missionId = missionId;
	}

	public String getSatelliteId() {
		return satelliteId;
	}

	public void setSatelliteId(final String satelliteId) {
		this.satelliteId = satelliteId;
	}

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(final String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(final ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}
}
