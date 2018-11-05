package esa.s1pdgs.cpoc.jobgenerator.model;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class describing the content of an EDRS session file<br/>
 * This class is the mapping of the XML EDRS session file<br/>
 * It is also used as internal object to store sessions
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "DCSU_Session_Information_Block")
@XmlAccessorType(XmlAccessType.NONE)
public class EdrsSessionFile {
    
    public final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

	/**
	 * Session identifier
	 */
	@XmlElement(name = "session_id")
	private String sessionId;

	/**
	 * Acquisition start time
	 */
	@XmlElement(name = "time_start")
	private String startTime;

	/**
	 * Acquisition stop time
	 */
	@XmlElement(name = "time_stop")
	private String stopTime;

	/**
	 * List of raw names
	 */
	@XmlElementWrapper(name = "dsdb_list")
	@XmlElement(name = "dsdb_name")
	private List<EdrsSessionFileRaw> rawNames;

	/**
	 * Default constructor
	 */
	public EdrsSessionFile() {
		this.rawNames = new ArrayList<>();
	}

	/**
	 * Constructor using fields
	 * 
	 * @param sessionId
	 * @param startTime
	 * @param stopTime
	 * @param rawNames
	 */
	public EdrsSessionFile(final String sessionId, final String startTime, final String stopTime,
			final List<EdrsSessionFileRaw> rawNames) {
		this();
		this.sessionId = sessionId;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.rawNames.addAll(rawNames);
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the rawNames
	 */
	public List<EdrsSessionFileRaw> getRawNames() {
		return rawNames;
	}

	/**
	 * @param rawNames
	 *            the rawNames to set
	 */
	public void setRawNames(final List<EdrsSessionFileRaw> rawNames) {
		this.rawNames = rawNames;
	}

	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
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
	 * @param stopTime
	 *            the stopTime to set
	 */
	public void setStopTime(final String stopTime) {
		this.stopTime = stopTime;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{sessionId: %s, startTime: %s, stopTime: %s, rawNames: %s}", sessionId, startTime,
				stopTime, rawNames);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(sessionId, startTime, stopTime, rawNames);
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
			EdrsSessionFile other = (EdrsSessionFile) obj;
			ret = Objects.equals(sessionId, other.sessionId) && Objects.equals(startTime, other.startTime)
					&& Objects.equals(stopTime, other.stopTime) && Objects.equals(rawNames, other.rawNames);
		}
		return ret;
	}

}
