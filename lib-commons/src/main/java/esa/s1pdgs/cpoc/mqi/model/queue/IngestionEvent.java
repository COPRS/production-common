package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * DTO object used to transfer EDRS session files between MQI and application
 * 
 * @author Viveris technologies
 */
public class IngestionEvent extends AbstractDto {

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

    /**
     * Default constructor
     */
    public IngestionEvent(final String objectStorageKey, String inboxPath, final int channelId,
            final EdrsSessionFileType productType, final String missionId,
            final String satelliteId, final String stationCode, final String sessionId) {
        super(objectStorageKey, ProductFamily.EDRS_SESSION);
        this.inboxPath = inboxPath;
        this.channelId = channelId;
        this.productType = productType;
        this.missionId = missionId;
        this.satelliteId = satelliteId;
        this.stationCode = stationCode;
        this.sessionId = sessionId;
    }

    /**
     * @return the objectStorageKey
     */
    public String getKeyObjectStorage() {
        return getProductName();
    }

    /**
     * @param objectStorageKey
     *            the objectStorageKey to set
     */
    public void setKeyObjectStorage(final String keyObjectStorage) {
        this.setProductName(keyObjectStorage);
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
	public void setInboxPath(String inboxPath) {
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
	public void setStationCode(String stationCode) {
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
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{objectStorageKey: %s, inboxPath: %s, channelId: %s, productType: %s, satelliteId: %s, missionId: %s, stationCode: %s, sessionId: %s, hostname: %s, creationDate: %s}",
                getKeyObjectStorage(), inboxPath, channelId, productType, satelliteId,
                missionId, stationCode, sessionId, getHostname(), getCreationDate());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(getKeyObjectStorage(), inboxPath, getFamily(), channelId, productType,
                satelliteId, missionId, stationCode, sessionId, getHostname(), getCreationDate());
    }

    /**
     * @see java.lang.Object#equals()
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            IngestionEvent other = (IngestionEvent) obj;
            // field comparison
            ret = Objects.equals(getKeyObjectStorage(), other.getKeyObjectStorage())
            		&& Objects.equals(inboxPath, other.inboxPath)
            		&&  Objects.equals(getFamily(), other.getFamily())
                    && channelId == other.channelId
                    && Objects.equals(productType, other.productType)
                    && Objects.equals(satelliteId, other.satelliteId)
                    && Objects.equals(missionId, other.missionId)
                    && Objects.equals(stationCode, other.stationCode)
            		&& Objects.equals(sessionId, other.sessionId)
            		&& Objects.equals(getHostname(), other.getHostname())
            		&& Objects.equals(getCreationDate(), other.getCreationDate());
        }
        return ret;
    }

}
