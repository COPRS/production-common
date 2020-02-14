package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class IngestionJob extends AbstractMessage {	
	private String relativePath;
	private String pickupPath;
	
	public IngestionJob() {
		super();
	}

	public IngestionJob(final String keyObjectStorage) {
		super(ProductFamily.BLANK, keyObjectStorage);
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}


	public String getPickupPath() {
		return pickupPath;
	}

	public void setPickupPath(final String pickupPath) {
		this.pickupPath = pickupPath;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, pickupPath, productFamily,
				relativePath, uid);
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
				&& Objects.equals(pickupPath, other.pickupPath)
				&& productFamily == other.productFamily 
				&& Objects.equals(uid, other.uid)
				&& Objects.equals(relativePath, other.relativePath);
	}

	@Override
	public String toString() {
		return "IngestionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + relativePath
				+ ", pickupPath=" + pickupPath + ", uid=" + uid +"]";
	}	
}
