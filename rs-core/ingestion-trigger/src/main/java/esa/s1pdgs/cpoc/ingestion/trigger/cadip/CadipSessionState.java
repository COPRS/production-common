/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
@Document(collection = "cadipSessionState")
public class CadipSessionState {

	@Id
	private ObjectId id; // necessary for repository.delete(entry)

	private String pod;
	private String cadipUrl;
	private String sessionId;
	private boolean retransfer;
	private Date nextWindowStart;
	private Integer numChannels;
	private Integer completedChannels;

	@Override
	public String toString() {
		return "CadipSessionState [id=" + id + ", pod=" + pod + ", cadipUrl=" + cadipUrl + ", sessionId=" + sessionId
				+ ", retransfer=" + retransfer + ", nextWindowStart=" + nextWindowStart + ", numChannels=" + numChannels
				+ ", completedChannels=" + completedChannels + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cadipUrl, completedChannels, id, nextWindowStart, numChannels, pod, retransfer, sessionId);
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
				&& retransfer == other.retransfer && Objects.equals(sessionId, other.sessionId);
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

	public boolean isRetransfer() {
		return retransfer;
	}

	public void setRetransfer(boolean retransfer) {
		this.retransfer = retransfer;
	}
		
}
