package esa.s1pdgs.cpoc.metadata.model;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Class describing the metadata of a file
 * 
 * @author Cyrielle Gailliard
 */
public abstract class AbstractMetadata {

	public final static DateTimeFormatter METADATA_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
	
    /**
     * Product name
     */
    protected String productName;

    /**
     * Product type
     */
    protected String productType;

    /**
     * Key in object storage
     */
    protected String keyObjectStorage;

    /**
     * Validity start time
     */
    protected String validityStart;

    /**
     * Validity stop time
     */
    protected String validityStop;
    
    /**
     * Mission ID
     */
    protected String missionId;
    
    /**
     * Satellite ID
     */
    protected String satelliteId;
    
    /**
     * Station Code
     */
    protected String stationCode;
    
    /**
     * Swath Type
     */
    protected String swathtype;

    protected Map<String, String> additionalProperties = new HashMap<>();
    
    /**
     * @param productName
     * @param productType
     * @param keyObjectStorage
     * @param validityStart
     * @param validityStop
     * @param missionId
     * @param satelliteId
     * @param stationCode
     */
    public AbstractMetadata(final String productName, final String productType,
            final String keyObjectStorage, final String validityStart,
            final String validityStop, final String missionId, final String satelliteId, final String stationCode) {
        super();
        this.productName = productName;
        this.productType = productType;
        this.keyObjectStorage = keyObjectStorage;
        this.validityStart = validityStart;
        this.validityStop = validityStop;
        this.missionId =  missionId;
        this.satelliteId = satelliteId;
        this.stationCode = stationCode;
    }
    
    public AbstractMetadata() {
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName
     *            the productName to set
     */
    public void setProductName(final String productName) {
        this.productName = productName;
    }

    /**
     * @return the keyObjectStorage
     */
    public String getKeyObjectStorage() {
        return keyObjectStorage;
    }

    /**
     * @param keyObjectStorage
     *            the keyObjectStorage to set
     */
    public void setKeyObjectStorage(final String keyObjectStorage) {
        this.keyObjectStorage = keyObjectStorage;
    }

    /**
     * @return the validityStart
     */
    public String getValidityStart() {
        return validityStart;
    }

    /**
     * @param validityStart
     *            the validityStart to set
     */
    public void setValidityStart(final String validityStart) {
        this.validityStart = validityStart;
    }

    /**
     * @return the validityStop
     */
    public String getValidityStop() {
        return validityStop;
    }

    /**
     * @param validityStop
     *            the validityStop to set
     */
    public void setValidityStop(final String validityStop) {
        this.validityStop = validityStop;
    }

    /**
     * @return the productType
     */
    public String getProductType() {
        return productType;
    }

    /**
     * @param productType
     *            the productType to set
     */
    public void setProductType(final String productType) {
        this.productType = productType;
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
	 * @param satelliteId the satelliteId to set
	 */
	public void setSatelliteId(final String satelliteId) {
		this.satelliteId = satelliteId;
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

	public String getSwathtype() {
		return swathtype;
	}
	
	public void setSwathtype(final String swathtype) {
		this.swathtype = swathtype;
	}

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(final Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public void addAdditionalProperty(final String key, final String value) {
	    additionalProperties.put(key, value);
    }

    public String toAbstractString() {
		return String.format(
				"\"productName\":\"%s\",\"productType\":\"%s\",\"keyObjectStorage\":\"%s\",\"validityStart\":\"%s\",\"validityStop\":\"%s\",\"missionId\":\"%s\",\"satelliteId\":\"%s\",\"stationCode\":\"%s\",\"swathtype\":\"%s\"",
				productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode, swathtype);
	}
}
