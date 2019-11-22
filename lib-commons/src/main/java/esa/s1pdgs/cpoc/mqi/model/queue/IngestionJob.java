package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class IngestionJob extends AbstractMessage {
	
	private ProductFamily productFamily;
	private String keyObjectStorage;

	private String relativePath;
	private String pickupPath;
	private String missionId;
	private String satelliteId;
	private String stationCode;
	
	public IngestionJob() {
		
	}

	public IngestionJob(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
		this.productFamily = ProductFamily.BLANK;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
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

	public String getStationCode() {
		return stationCode;
	}

	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}

	public String getPickupPath() {
		return pickupPath;
	}

	public void setPickupPath(String pickupPath) {
		this.pickupPath = pickupPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getKeyObjectStorage() == null) ? 0 : getKeyObjectStorage().hashCode());
		result = prime * result + ((getProductFamily() == null) ? 0 : getProductFamily().hashCode());
		result = prime * result + ((missionId == null) ? 0 : missionId.hashCode());
		result = prime * result + ((pickupPath == null) ? 0 : pickupPath.hashCode());
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
		result = prime * result + ((satelliteId == null) ? 0 : satelliteId.hashCode());
		result = prime * result + ((stationCode == null) ? 0 : stationCode.hashCode());
		result = prime * result + ((getHostname() == null) ? 0 : getHostname().hashCode());
		result = prime * result + ((getCreationDate() == null) ? 0 : getCreationDate().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IngestionJob other = (IngestionJob) obj;
		if (missionId == null) {
			if (other.missionId != null)
				return false;
		} else if (!missionId.equals(other.missionId))
			return false;
		if (getKeyObjectStorage() == null) {
			if (other.getKeyObjectStorage() != null)
				return false;
		}
		if (getProductFamily() == null) {
			if (other.getProductFamily() != null)
				return false;
		} else if (!getProductFamily().equals(other.getProductFamily()))
			return false;
		if (pickupPath == null) {
			if (other.pickupPath != null)
				return false;
		} else if (!pickupPath.equals(other.pickupPath))
			return false;
		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		} else if (!relativePath.equals(other.relativePath))
			return false;
		if (satelliteId == null) {
			if (other.satelliteId != null)
				return false;
		} else if (!satelliteId.equals(other.satelliteId))
			return false;
		if (stationCode == null) {
			if (other.stationCode != null)
				return false;
		} else if (!stationCode.equals(other.stationCode))
			return false;
		if (getHostname() == null) {
			if (other.getHostname() != null)
				return false;
		} else if (!getHostname().equals(other.getHostname()))
			return false;
		if (getCreationDate() == null) {
			if (other.getCreationDate() != null)
				return false;
		} else if (!getCreationDate().equals(other.getCreationDate()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"IngestionJob [relativePath=%s, pickupPath=%s, missionId=%s, satelliteId=%s, stationCode=%s, hostname: %s, creationDate: %s]",
				relativePath, pickupPath, missionId, satelliteId, stationCode, getHostname(), getCreationDate());
	}
}
