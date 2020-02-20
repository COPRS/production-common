package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

public class CatalogJob extends AbstractMessage {	
	private String productName = NOT_DEFINED;
	private String relativePath = NOT_DEFINED;
	private String mode = "NOMINAL";
    private OQCFlag oqcFlag = OQCFlag.NOT_CHECKED;
    private String timeliness;
    
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

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public OQCFlag getOqcFlag() {
		return oqcFlag;
	}

	public void setOqcFlag(final OQCFlag oqcFlag) {
		this.oqcFlag = oqcFlag;
	}

	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(String timeliness) {
		this.timeliness = timeliness;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, relativePath, mode, oqcFlag, timeliness,
				productFamily, productName, uid);
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
		final CatalogJob other = (CatalogJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(relativePath, other.relativePath) 
				&& Objects.equals(mode, other.mode)
				&& oqcFlag == other.oqcFlag 
				&& Objects.equals(timeliness, other.timeliness) 
				&& productFamily == other.productFamily
				&& Objects.equals(uid, other.uid)
				&& Objects.equals(productName, other.productName);
	}

	@Override
	public String toString() {
		return "CatalogJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productName=" + productName
				+ ", relativePath=" + relativePath + ", mode=" + mode + ", oqcFlag=" + oqcFlag
				+ ", timeliness=" + timeliness + ", uid=" + uid + "]";
	}

}
