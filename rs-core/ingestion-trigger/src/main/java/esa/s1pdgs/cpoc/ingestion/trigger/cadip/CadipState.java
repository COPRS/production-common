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
 * The CadipState persists the next time a given CADIP interface has to be
 * queried in order to retrieve new session objects.
 */
@Document(collection = "cadipState")
public class CadipState {

	@Id
	private ObjectId id; // necessary for repository.delete(entry)

	private Date nextWindowStart;
	private String pod;
	private String cadipUrl;
	private String satelliteId;

	@Override
	public String toString() {
		return "CadipState [id=" + id + ", nextWindowStart=" + nextWindowStart + ", pod=" + pod + ", cadipUrl="
				+ cadipUrl + ", satelliteId=" + satelliteId + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cadipUrl, id, nextWindowStart, pod, satelliteId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CadipState other = (CadipState) obj;
		return Objects.equals(cadipUrl, other.cadipUrl) && Objects.equals(id, other.id)
				&& Objects.equals(nextWindowStart, other.nextWindowStart) && Objects.equals(pod, other.pod)
				&& Objects.equals(satelliteId, other.satelliteId);
	}

	public Date getNextWindowStart() {
		return nextWindowStart;
	}

	public void setNextWindowStart(Date nextWindowStart) {
		this.nextWindowStart = nextWindowStart;
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

	public String getSatelliteId() {
		return satelliteId;
	}

	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
	}
}
