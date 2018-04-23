package fr.viveris.s1pdgs.jobgenerator.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class describing the content of an EDRS session file
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "DCSU_Session_Information_Block")
@XmlAccessorType(XmlAccessType.NONE)
public class EdrsSessionFile {

	/**
	 * Session identifier
	 */
	@XmlElement(name = "session_id")
	private String sessionId;

	/**
	 * Acquisition start time
	 */
	@XmlElement(name = "time_start")
	private Date startTime;

	/**
	 * Acquisition stop time
	 */
	@XmlElement(name = "time_stop")
	private Date stopTime;

	/**
	 * List of raw names
	 */
	@XmlElementWrapper(name = "dsdb_list")
	@XmlElement(name = "dsdb_name")
	private List<EdrsSessionFileRaw> rawNames;

	public EdrsSessionFile() {
		this.rawNames = new ArrayList<>();
	}

	/**
	 * @param sessionId
	 * @param startTime
	 * @param stopTime
	 * @param rawNames
	 */
	public EdrsSessionFile(String sessionId, Date startTime, Date stopTime, List<EdrsSessionFileRaw> rawNames) {
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
	public void setSessionId(String sessionId) {
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
	public void setRawNames(List<EdrsSessionFileRaw> rawNames) {
		this.rawNames = rawNames;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the stopTime
	 */
	public Date getStopTime() {
		return stopTime;
	}

	/**
	 * @param stopTime
	 *            the stopTime to set
	 */
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rawNames == null) ? 0 : rawNames.hashCode());
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((stopTime == null) ? 0 : stopTime.hashCode());
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
		EdrsSessionFile other = (EdrsSessionFile) obj;
		if (rawNames == null) {
			if (other.rawNames != null)
				return false;
		} else if (!rawNames.equals(other.rawNames))
			return false;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (stopTime == null) {
			if (other.stopTime != null)
				return false;
		} else if (!stopTime.equals(other.stopTime))
			return false;
		return true;
	}
	
}
