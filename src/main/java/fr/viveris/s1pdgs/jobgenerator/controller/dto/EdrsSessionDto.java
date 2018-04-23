package fr.viveris.s1pdgs.jobgenerator.controller.dto;

/**
 * Exchanged object in the topic t-pdgs-edrs-sessions
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionDto {

	/**
	 * Object storage key in the bucket of EDRS session files
	 */
	private String objectStorageKey;
	
	/**
	 * AbstractProduct type: SESSION or RAW
	 */
	private String productType;
	
	/**
	 * Channel identifier
	 */
	private int channelId;
	
	/**
	 * Mission identifier
	 */
	private String missionId;
	
	/**
	 * Satellite identifier
	 */
	private String satelliteId;

	/**
	 * Default constructor
	 */
	public EdrsSessionDto() {

	}

	/**
	 * Default constructor
	 */
	public EdrsSessionDto(String objectStorageKey, int channelId, String productType, String missionId, String satelliteId) {
		this();
		this.objectStorageKey = objectStorageKey;
		this.channelId = channelId;
		this.productType = productType;
		this.missionId = missionId;
		this.satelliteId = satelliteId;
	}

	/**
	 * @return the objectStorageKey
	 */
	public String getObjectStorageKey() {
		return objectStorageKey;
	}

	/**
	 * @param objectStorageKey
	 *            the objectStorageKey to set
	 */
	public void setObjectStorageKey(String objectStorageKey) {
		this.objectStorageKey = objectStorageKey;
	}

	/**
	 * @return the type
	 */
	public String getProductType() {
		return productType;
	}

	/**
	 * @param type the type to set
	 */
	public void setProductType(String productType) {
		this.productType = productType;
	}

	/**
	 * @return the channelId
	 */
	public int getChannelId() {
		return channelId;
	}

	/**
	 * @param channelId the channelId to set
	 */
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	/**
	 * @return the missionId
	 */
	public String getMissionId() {
		return missionId;
	}

	/**
	 * @param missionId the missionId to set
	 */
	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	/**
	 * @return the satelliteId
	 */
	public String getSatelliteId() {
		return satelliteId;
	}

	/**
	 * @param satelliteId the satelliteId to set
	 */
	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EdrsSessionDto [objectStorageKey=" + objectStorageKey + ", productType=" + productType + ", channelId="
				+ channelId + ", missionId=" + missionId + ", satelliteId=" + satelliteId + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channelId;
		result = prime * result + ((missionId == null) ? 0 : missionId.hashCode());
		result = prime * result + ((objectStorageKey == null) ? 0 : objectStorageKey.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((satelliteId == null) ? 0 : satelliteId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdrsSessionDto other = (EdrsSessionDto) obj;
		if (channelId != other.channelId)
			return false;
		if (missionId == null) {
			if (other.missionId != null)
				return false;
		} else if (!missionId.equals(other.missionId))
			return false;
		if (objectStorageKey == null) {
			if (other.objectStorageKey != null)
				return false;
		} else if (!objectStorageKey.equals(other.objectStorageKey))
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
		return true;
	}

}
