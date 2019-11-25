package esa.s1pdgs.cpoc.mqi.model.queue;

import java.time.LocalDateTime;
import java.util.Objects;

public class CatalogEvent extends AbstractMessage {		
	private String productType;
	private String missionId;
	private String satelliteId;
	private LocalDateTime creationTime;
	private LocalDateTime insertionTime;
	private LocalDateTime validityStartTime;
	private LocalDateTime validityStopTime;
	private String instrumentConfigurationId;
	private String site;
	private String url;
	
	public String getProductType() {
		return productType;
	}

	public void setProductType(final String productType) {
		this.productType = productType;
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

	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final LocalDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public LocalDateTime getInsertionTime() {
		return insertionTime;
	}

	public void setInsertionTime(final LocalDateTime insertionTime) {
		this.insertionTime = insertionTime;
	}

	public LocalDateTime getValidityStartTime() {
		return validityStartTime;
	}

	public void setValidityStartTime(final LocalDateTime validityStartTime) {
		this.validityStartTime = validityStartTime;
	}

	public LocalDateTime getValidityStopTime() {
		return validityStopTime;
	}

	public void setValidityStopTime(final LocalDateTime validityStopTime) {
		this.validityStopTime = validityStopTime;
	}

	public String getInstrumentConfigurationId() {
		return instrumentConfigurationId;
	}

	public void setInstrumentConfigurationId(final String instrumentConfigurationId) {
		this.instrumentConfigurationId = instrumentConfigurationId;
	}

	public String getSite() {
		return site;
	}

	public void setSite(final String site) {
		this.site = site;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, creationTime, hostname, insertionTime, instrumentConfigurationId,
				keyObjectStorage, missionId, productFamily, productType, satelliteId, site, url, validityStartTime,
				validityStopTime);
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
		final CatalogEvent other = (CatalogEvent) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(creationTime, other.creationTime)
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(insertionTime, other.insertionTime)
				&& Objects.equals(instrumentConfigurationId, other.instrumentConfigurationId)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) 
				&& productFamily == other.productFamily
				&& Objects.equals(productType, other.productType) 
				&& Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(site, other.site) 
				&& Objects.equals(url, other.url)
				&& Objects.equals(validityStartTime, other.validityStartTime)
				&& Objects.equals(validityStopTime, other.validityStopTime);
	}

	@Override
	public String toString() {
		return "CatalogEvent [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productType=" + productType
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", creationTime=" + creationTime
				+ ", insertionTime=" + insertionTime + ", validityStartTime=" + validityStartTime
				+ ", validityStopTime=" + validityStopTime + ", instrumentConfigurationId=" + instrumentConfigurationId
				+ ", site=" + site + ", url=" + url + "]";
	}
}