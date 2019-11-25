package esa.s1pdgs.cpoc.mqi.model.queue;

import java.time.LocalDateTime;

import esa.s1pdgs.cpoc.common.ProductFamily;

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
	
	public CatalogEvent() {
		super();
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
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

	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(LocalDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public LocalDateTime getInsertionTime() {
		return insertionTime;
	}

	public void setInsertionTime(LocalDateTime insertionTime) {
		this.insertionTime = insertionTime;
	}

	public LocalDateTime getValidityStartTime() {
		return validityStartTime;
	}

	public void setValidityStartTime(LocalDateTime validityStartTime) {
		this.validityStartTime = validityStartTime;
	}

	public LocalDateTime getValidityStopTime() {
		return validityStopTime;
	}

	public void setValidityStopTime(LocalDateTime validityStopTime) {
		this.validityStopTime = validityStopTime;
	}

	public String getInstrumentConfigurationId() {
		return instrumentConfigurationId;
	}

	public void setInstrumentConfigurationId(String instrumentConfigurationId) {
		this.instrumentConfigurationId = instrumentConfigurationId;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "CatalogEvent [productType=" + productType + ", missionId=" + missionId + ", satelliteId=" + satelliteId
				+ ", creationTime=" + creationTime + ", insertionTime=" + insertionTime + ", validityStartTime="
				+ validityStartTime + ", validityStopTime=" + validityStopTime + ", instrumentConfigurationId="
				+ instrumentConfigurationId + ", site=" + site + ", url=" + url + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((creationTime == null) ? 0 : creationTime.hashCode());
		result = prime * result + ((insertionTime == null) ? 0 : insertionTime.hashCode());
		result = prime * result + ((instrumentConfigurationId == null) ? 0 : instrumentConfigurationId.hashCode());
		result = prime * result + ((missionId == null) ? 0 : missionId.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((satelliteId == null) ? 0 : satelliteId.hashCode());
		result = prime * result + ((site == null) ? 0 : site.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((validityStartTime == null) ? 0 : validityStartTime.hashCode());
		result = prime * result + ((validityStopTime == null) ? 0 : validityStopTime.hashCode());
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
		CatalogEvent other = (CatalogEvent) obj;
		if (creationTime == null) {
			if (other.creationTime != null)
				return false;
		} else if (!creationTime.equals(other.creationTime))
			return false;
		if (insertionTime == null) {
			if (other.insertionTime != null)
				return false;
		} else if (!insertionTime.equals(other.insertionTime))
			return false;
		if (instrumentConfigurationId == null) {
			if (other.instrumentConfigurationId != null)
				return false;
		} else if (!instrumentConfigurationId.equals(other.instrumentConfigurationId))
			return false;
		if (missionId == null) {
			if (other.missionId != null)
				return false;
		} else if (!missionId.equals(other.missionId))
			return false;
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		if (satelliteId == null) {
			if (other.satelliteId != null)
				return false;
		} else if (!satelliteId.equals(other.satelliteId))
			return false;
		if (site == null) {
			if (other.site != null)
				return false;
		} else if (!site.equals(other.site))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (validityStartTime == null) {
			if (other.validityStartTime != null)
				return false;
		} else if (!validityStartTime.equals(other.validityStartTime))
			return false;
		if (validityStopTime == null) {
			if (other.validityStopTime != null)
				return false;
		} else if (!validityStopTime.equals(other.validityStopTime))
			return false;
		return true;
	}
	
}