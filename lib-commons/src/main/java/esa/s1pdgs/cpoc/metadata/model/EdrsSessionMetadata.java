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

	/**
     * Start time
     */
    protected String startTime;
    
    /**
     * Stop time
     */
    protected String stopTime;
    
    /**
     * Raw names
     */
    protected List<String> rawNames;
    
	/**
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param startTime
	 * @param stopTime
	 * @param validityStart
	 * @param validityStop
	 * @param missionId
     * @param satelliteId
     * @param stationCode
     * @param rawNames
	 */
	public EdrsSessionMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String startTime, final String stopTime, final String validityStart, final String validityStop,
			final String missionId, final String satelliteId, final String stationCode, final List<String> rawNames) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
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
	public void setStartTime(String startTime) {
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
	public void setStopTime(String stopTime) {
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
	public void setRawNames(List<String> rawNames) {
		this.rawNames = rawNames;
	}
	
	@Override
	public String toString() {
		String superToString = super.toAbstractString();
		return String.format("{%s,\"startTime\":\"%s\",\"stopTime\":\"%s\",\"rawNames\":[%s]}", superToString, startTime, stopTime, rawNames.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")));
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), startTime, stopTime, rawNames);
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
			EdrsSessionMetadata other = (EdrsSessionMetadata) obj;
			ret = super.equals(other) && Objects.equals(startTime, other.startTime) && Objects.equals(stopTime, other.stopTime)
					&& Objects.equals(rawNames, other.rawNames);
		}
		return ret;
	}
}
