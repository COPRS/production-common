package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CatalogEvent extends AbstractDto {
	
	private String productType;
	private String missionId;
	private String satelliteId;
	private String creationTime;
	private String insertionTime;
	private String validityStartTime;
	private String validityStopTime;
	private String instrumentConfigurationId;
	private String site;
	private String url;
	
	public CatalogEvent() {
		super();
	}
	
	public CatalogEvent(String productName, ProductFamily family) {
		super(productName, family);
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

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	public String getInsertionTime() {
		return insertionTime;
	}

	public void setInsertionTime(String insertionTime) {
		this.insertionTime = insertionTime;
	}

	public String getValidityStartTime() {
		return validityStartTime;
	}

	public void setValidityStartTime(String validityStartTime) {
		this.validityStartTime = validityStartTime;
	}

	public String getValidityStopTime() {
		return validityStopTime;
	}

	public void setValidityStopTime(String validityStopTime) {
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
	
}