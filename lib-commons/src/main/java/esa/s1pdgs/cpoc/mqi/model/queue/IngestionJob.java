package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class IngestionJob extends AbstractMessage {	
	
	/**
	 * either file:// or https://, base-URL of XBIP or inbox
	 */
	private String pickupBaseURL;

	/**
	 * path to file/folder relative to pickupBaseURL
	 */
	private String relativePath;
	
	/**
	 * name of file/folder to be ingested (to be used by IngestionWorker instead of keyObjectStorage
	 */
	private String productName;
	
	
	public IngestionJob() {
		super();
	}

	public IngestionJob(final ProductFamily family, final String keyObjectStorage) {
		super(family, keyObjectStorage);
	}

	public String getPickupBaseURL() {
		return pickupBaseURL;
	}

	public void setPickupBaseURL(final String pickupBaseURL) {
		this.pickupBaseURL = pickupBaseURL;
	}
	
	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, pickupBaseURL, productFamily,
				relativePath, productName, uid);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IngestionJob other = (IngestionJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(pickupBaseURL, other.pickupBaseURL)
				&& productFamily == other.productFamily 
				&& Objects.equals(uid, other.uid)
				&& Objects.equals(relativePath, other.relativePath)
				&& Objects.equals(productName, other.productName);
	}

	@Override
	public String toString() {
		return "IngestionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + relativePath
				+ ", pickupBaseURL=" + pickupBaseURL + ", productName=" + productName + ", uid=" + uid +"]";
	}	
}
