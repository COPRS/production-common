package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * DTO object used to transfer EDRS session files between MQI and application
 * 
 * @author Viveris technologies
 */
public class IngestionEvent extends AbstractMessage {
    /**
     * Channel identifier
     */
    private int channelId;
    
    /**
     * Type of the EDRS session file: raw or XML file
     */
    private EdrsSessionFileType productType;

    /**
     * Session id
     */
    private String sessionId;
    
    /**
     * Satellite identifier
     */
    private String satelliteId;

    /**
     * Mission identifier
     */
    private String missionId;
    
    /**
     * Station code
     */
    private String stationCode;

    /**
     * Inbox path
     */
    private String inboxPath;
    
    /**
     * Default constructor
     */
    public IngestionEvent() {
        super();
    }

    public IngestionEvent(
    		final String keyObjectStorage, 
    		final String inboxPath, 
    		final int channelId,
            final EdrsSessionFileType productType, 
            final String missionId,
            final String satelliteId, 
            final String stationCode, 
            final String sessionId
    ) {
        super(ProductFamily.EDRS_SESSION, keyObjectStorage);
        this.inboxPath = inboxPath;
        this.channelId = channelId;
        this.productType = productType;
        this.missionId = missionId;
        this.satelliteId = satelliteId;
        this.stationCode = stationCode;
        this.sessionId = sessionId;
    }

	/**
	 * @return the inboxPath
	 */
	public String getInboxPath() {
		return inboxPath;
	}

	/**
	 * @param inboxPath the inboxPath to set
	 */
	public void setInboxPath(final String inboxPath) {
		this.inboxPath = inboxPath;
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
     * @return the productType
     */
    public EdrsSessionFileType getProductType() {
        return productType;
    }

    /**
     * @param productType
     *            the productType to set
     */
    public void setProductType(final EdrsSessionFileType productType) {
        this.productType = productType;
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
	 * @return the stationCode
	 */
	public String getStationCode() {
		return stationCode;
	}

	/**
	 * @param stationCode the stationCode to set
	 */
	public void setStationCode(final String stationCode) {
		this.stationCode = stationCode;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(channelId, creationDate, hostname, inboxPath, keyObjectStorage, missionId, productFamily,
				productType, satelliteId, sessionId, stationCode);
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
		final IngestionEvent other = (IngestionEvent) obj;
		return channelId == other.channelId 
				&& Objects.equals(creationDate, other.creationDate)
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(inboxPath, other.inboxPath)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) 
				&& productFamily == other.productFamily
				&& productType == other.productType 
				&& Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(sessionId, other.sessionId) 
				&& Objects.equals(stationCode, other.stationCode);
	}

	@Override
	public String toString() {
		return "IngestionEvent [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", channelId=" + channelId
				+ ", productType=" + productType + ", sessionId=" + sessionId + ", satelliteId=" + satelliteId
				+ ", missionId=" + missionId + ", stationCode=" + stationCode + ", inboxPath=" + inboxPath + "]";
	}
}
