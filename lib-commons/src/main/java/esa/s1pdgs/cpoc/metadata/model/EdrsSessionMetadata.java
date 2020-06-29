package esa.s1pdgs.cpoc.metadata.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionMetadata extends AbstractMetadata {

    protected String sessionId;
    protected String startTime;
    protected String stopTime;
    protected List<String> rawNames = Collections.emptyList();
    private int channelId;
    
	/**
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param sessionId
	 * @param startTime
	 * @param stopTime
	 * @param validityStart
	 * @param validityStop
	 * @param missionId
     * @param satelliteId
     * @param stationCode
     * @param rawNames
	 */
	public EdrsSessionMetadata(final String productName, final String productType, final String keyObjectStorage, final String sessionId,
			final String startTime, final String stopTime, final String validityStart, final String validityStop,
			final String missionId, final String satelliteId, final String stationCode, final List<String> rawNames) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
		this.sessionId = sessionId;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.rawNames = null == rawNames ? Collections.emptyList() : rawNames;
	}
	
	public EdrsSessionMetadata() {
		super();
	}
	
	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(final String startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the stopTime
	 */
	public String getStopTime() {
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(final String stopTime) {
		this.stopTime = stopTime;
	}

	/**
	 * @return the rawNames
	 */
	public List<String> getRawNames() {
		return rawNames;
	}

	/**
	 * @param rawNames the rawNames to set
	 */
	public void setRawNames(final List<String> rawNames) {
		this.rawNames = rawNames;
	}

	
	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}
	
	
	
	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(final int channelId) {
		this.channelId = channelId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String toJsonString() {
		final String superToString = super.toAbstractString();
		return String.format("{%s,\"sessionId\":\"%s\",\"startTime\":\"%s\",\"stopTime\":\"%s\",\"rawNames\":[%s]}", superToString, sessionId, startTime, stopTime, rawNames.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")));
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), sessionId, startTime, stopTime, rawNames, channelId);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			final EdrsSessionMetadata other = (EdrsSessionMetadata) obj;
			ret = super.equals(other) && 
					Objects.equals(sessionId, other.sessionId) &&
					Objects.equals(startTime, other.startTime) && 
					Objects.equals(stopTime, other.stopTime)
					&& Objects.equals(rawNames, other.rawNames) && 
					channelId == other.channelId;
		}
		return ret;
	}

	@Override
	public String toString() {
		return "EdrsSessionMetadata [startTime=" + startTime + ", stopTime=" + stopTime + ", rawNames=" + rawNames
				+ ", productName=" + productName + ", productType=" + productType + ", keyObjectStorage="
				+ keyObjectStorage + ", validityStart=" + validityStart + ", validityStop=" + validityStop
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", stationCode=" + stationCode + "]";
	}
}
