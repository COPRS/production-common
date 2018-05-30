package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import java.util.Objects;

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
		super();
	}

	/**
	 * Default constructor
	 */
	public EdrsSessionDto(final String objectStorageKey, final int channelId, final String productType,
			final String missionId, final String satelliteId) {
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
	public void setObjectStorageKey(final String objectStorageKey) {
		this.objectStorageKey = objectStorageKey;
	}

	/**
	 * @return the type
	 */
	public String getProductType() {
		return productType;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setProductType(final String productType) {
		this.productType = productType;
	}

	/**
	 * @return the channelId
	 */
	public int getChannelId() {
		return channelId;
	}

	/**
	 * @param channelId
	 *            the channelId to set
	 */
	public void setChannelId(final int channelId) {
		this.channelId = channelId;
	}

	/**
	 * @return the missionId
	 */
	public String getMissionId() {
		return missionId;
	}

	/**
	 * @param missionId
	 *            the missionId to set
	 */
	public void setMissionId(final String missionId) {
		this.missionId = missionId;
	}

	/**
	 * @return the satelliteId
	 */
	public String getSatelliteId() {
		return satelliteId;
	}

	/**
	 * @param satelliteId
	 *            the satelliteId to set
	 */
	public void setSatelliteId(final String satelliteId) {
		this.satelliteId = satelliteId;
	}

	/**
	 * To string
	 */
	@Override
	public String toString() {
		return String.format("{objectStorageKey: %s, productType: %s, channelId: %s, missionId: %s, satelliteId: %s}",
				objectStorageKey, productType, channelId, missionId, satelliteId);
	}

	/**
	 * Hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(channelId, missionId, objectStorageKey, productType, satelliteId);
	}

	/**
	 * Equals
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			EdrsSessionDto other = (EdrsSessionDto) obj;
			ret = channelId == other.channelId && Objects.equals(missionId, other.missionId)
					&& Objects.equals(objectStorageKey, other.objectStorageKey)
					&& Objects.equals(productType, other.productType) && Objects.equals(satelliteId, other.satelliteId);
		}
		return ret;
	}

}
