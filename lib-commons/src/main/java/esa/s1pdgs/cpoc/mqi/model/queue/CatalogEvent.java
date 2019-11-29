package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CatalogEvent extends AbstractMessage {		
	private String productType;
	private String missionId;
	private String satelliteId;
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	private Date creationTime;
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	private Date insertionTime;
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	private Date validityStartTime;
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	private Date validityStopTime;
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

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getInsertionTime() {
		return insertionTime;
	}

	public void setInsertionTime(final Date insertionTime) {
		this.insertionTime = insertionTime;
	}

	public Date getValidityStartTime() {
		return validityStartTime;
	}

	public void setValidityStartTime(final Date validityStartTime) {
		this.validityStartTime = validityStartTime;
	}

	public Date getValidityStopTime() {
		return validityStopTime;
	}

	public void setValidityStopTime(final Date validityStopTime) {
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

	public int getChannelId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStationCode() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setChannelId(final int i) {
		// TODO Auto-generated method stub
		
	}
}