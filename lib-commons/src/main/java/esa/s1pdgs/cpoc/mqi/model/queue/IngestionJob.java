package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class IngestionJob extends AbstractMessage {	
	private String relativePath;
	private String pickupPath;
	private String missionId;
	private String satelliteId;
	private String stationCode;
	
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

	public String getStationCode() {
		return stationCode;
	}

	public void setStationCode(final String stationCode) {
		this.stationCode = stationCode;
	}

	public String getPickupPath() {
		return pickupPath;
	}

	public void setPickupPath(final String pickupPath) {
		this.pickupPath = pickupPath;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, missionId, pickupPath, productFamily,
				relativePath, satelliteId, stationCode);
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
				&& Objects.equals(missionId, other.missionId) 
				&& Objects.equals(pickupPath, other.pickupPath)
				&& productFamily == other.productFamily 
				&& Objects.equals(relativePath, other.relativePath)
				&& Objects.equals(satelliteId, other.satelliteId) 
				&& Objects.equals(stationCode, other.stationCode);
	}

	@Override
	public String toString() {
		return "IngestionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + relativePath
				+ ", pickupPath=" + pickupPath + ", missionId=" + missionId + ", satelliteId=" + satelliteId
				+ ", stationCode=" + stationCode + "]";
	}	
}
