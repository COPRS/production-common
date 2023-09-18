package esa.s1pdgs.cpoc.ingestion.trigger.cadip;

import java.util.Date;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The CadipSessionState persists the state of each retrieved session. When a
 * session was retrieved completely, it can be deleted from this repository in
 * order to keep the amount of entries and file space low.
 */
@Document(collection = "cadipState")
public class CadipSessionState {

	@Id
	private ObjectId id; // necessary for repository.delete(entry)

	private String pod;
	private String cadipUrl;
	private String sessionId;
	private Date nextWindowStart;
	private Integer numChannels;
	private Integer completedChannels;

	@Override
	public String toString() {
		return "CadipSessionState [id=" + id + ", pod=" + pod + ", cadipUrl=" + cadipUrl + ", sessionId=" + sessionId
				+ ", nextWindowStart=" + nextWindowStart + ", numChannels=" + numChannels + ", completedChannels="
				+ completedChannels + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cadipUrl, completedChannels, id, nextWindowStart, numChannels, pod, sessionId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CadipSessionState other = (CadipSessionState) obj;
		return Objects.equals(cadipUrl, other.cadipUrl) && Objects.equals(completedChannels, other.completedChannels)
				&& Objects.equals(id, other.id) && Objects.equals(nextWindowStart, other.nextWindowStart)
				&& Objects.equals(numChannels, other.numChannels) && Objects.equals(pod, other.pod)
				&& Objects.equals(sessionId, other.sessionId);
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Date getNextWindowStart() {
		return nextWindowStart;
	}

	public void setNextWindowStart(Date nextWindowStart) {
		this.nextWindowStart = nextWindowStart;
	}

	public Integer getNumChannels() {
		return numChannels;
	}

	public void setNumChannels(Integer numChannels) {
		this.numChannels = numChannels;
	}

	public Integer getCompletedChannels() {
		return completedChannels;
	}

	public void setCompletedChannels(Integer completedChannels) {
		this.completedChannels = completedChannels;
	}

	public String getPod() {
		return pod;
	}

	public void setPod(String pod) {
		this.pod = pod;
	}

	public String getCadipUrl() {
		return cadipUrl;
	}

	public void setCadipUrl(String cadipUrl) {
		this.cadipUrl = cadipUrl;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
		
}
