package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

/**
 * DTO object used to transfer EDRS session files between MQI and application
 * 
 * @author Viveris technologies
 */
public class IngestionEvent extends AbstractMessage {
	private String productName = NOT_DEFINED;
	private String relativePath = NOT_DEFINED;

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, productFamily, productName, relativePath, uid);
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
		final IngestionEvent other = (IngestionEvent) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage) 
				&& productFamily == other.productFamily
				&& Objects.equals(productName, other.productName)
				&& Objects.equals(uid, other.uid)
				&& Objects.equals(relativePath, other.relativePath);
	}

	@Override
	public String toString() {
		return "IngestionEvent [productName=" + productName + ", productFamily=" + productFamily + ", keyObjectStorage=" 
				+ keyObjectStorage + ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + 
				relativePath + ", uid=" + uid +"]";
	}
}
