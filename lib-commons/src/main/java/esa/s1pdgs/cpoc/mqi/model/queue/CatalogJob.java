package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

public class CatalogJob extends AbstractMessage {	
	private String productName;
    private String sessionId;
    private String satelliteId;
    private String missionId;
    private String stationCode;
    private String mode;
    private OQCFlag oqcFlag = OQCFlag.NOT_CHECKED;

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSatelliteId() {
		return satelliteId;
	}

	public void setSatelliteId(final String satelliteId) {
		this.satelliteId = satelliteId;
	}

	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(final String missionId) {
		this.missionId = missionId;
	}

	public String getStationCode() {
		return stationCode;
	}

	public void setStationCode(final String stationCode) {
		this.stationCode = stationCode;
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

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, missionId, mode, oqcFlag, productFamily,
				productName, satelliteId, sessionId, stationCode);
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
				&& Objects.equals(missionId, other.missionId) 
				&& Objects.equals(mode, other.mode)
				&& oqcFlag == other.oqcFlag 
				&& productFamily == other.productFamily
				&& Objects.equals(productName, other.productName) 
				&& Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(sessionId, other.sessionId) 
				&& Objects.equals(stationCode, other.stationCode);
	}

	@Override
	public String toString() {
		return "CatalogJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productName=" + productName
				+ ", sessionId=" + sessionId + ", satelliteId=" + satelliteId + ", missionId=" + missionId
				+ ", stationCode=" + stationCode + ", mode=" + mode + ", oqcFlag=" + oqcFlag + "]";
	}
}
