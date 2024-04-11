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

package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import static java.lang.String.format;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Objects;

@Document(collection = "inboxEntry")
public class InboxEntry {

	@Transient
	public static final String ENTRY_SEQ_KEY = "inboxEntry";

	@Id
	private ObjectId id; //necessary for repository.delete(entry)

	private String name;
	private String relativePath;
	private String pickupURL;
	private Date lastModified; // ... timestamp of file on pickup
	private LocalDateTime knownSince;
	private long size;
	private String stationName;
	private String missionId;
	private String processingPod;
	private String inboxType;
	private String productFamily;

	// --------------------------------------------------------------------------

	public InboxEntry() {
	}

	public InboxEntry(final String name, final String relativePath, final String pickupURL, final Date lastModified,
			final long size, final String processingPod, final String inboxType, final String productFamily,
			final String stationName, final String missionId) {
		this.name = name;
		this.relativePath = relativePath;
		this.pickupURL = pickupURL;
		this.lastModified = lastModified;
		this.size = size;
		this.processingPod = processingPod;
		this.inboxType = inboxType;
		this.productFamily = productFamily;
		this.stationName = stationName;
		this.missionId = missionId;
	}

	// --------------------------------------------------------------------------

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		// WARNING: Don't take these attributes into account for equals/hashCode,
		// because they're not known on inbox side or may rightfully change
		// * id (only known in persistence)
		// * knownSince (only known in persistence)
		// * lastModified (may change in inbox, but don't matters in our context)
		final InboxEntry other = (InboxEntry) obj;
		return Objects.equals(this.name, other.name) && Objects.equals(this.pickupURL, other.pickupURL)
				&& Objects.equals(this.relativePath, other.relativePath)
				&& Objects.equals(this.stationName, other.stationName)
				&& Objects.equals(this.missionId, other.missionId)
				&& Objects.equals(this.processingPod, other.processingPod)
				&& Objects.equals(this.productFamily, other.productFamily)
				&& Objects.equals(this.inboxType, other.inboxType);
	}

	public boolean equalsWithoutProductFamily(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		// WARNING: Don't take these attributes into account for equals/hashCode,
		// because they're not known on inbox side or may rightfully change
		// * id (only known in persistence)
		// * knownSince (only known in persistence)
		// * lastModified (may change in inbox, but don't matters in our context)
		final InboxEntry other = (InboxEntry) obj;
		return Objects.equals(this.name, other.name) && Objects.equals(this.pickupURL, other.pickupURL)
				&& Objects.equals(this.relativePath, other.relativePath)
				&& Objects.equals(this.stationName, other.stationName)
				&& Objects.equals(this.missionId, other.missionId)
				&& Objects.equals(this.processingPod, other.processingPod)
				// omitting product family comparison S1PRO-2395
				&& Objects.equals(this.inboxType, other.inboxType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.pickupURL, this.relativePath, this.stationName, this.missionId, this.processingPod,
				this.inboxType, this.productFamily);
	}

	@Override
	public String toString() {
		return format(
				"InboxEntry [name=%s, relativePath=%s, pickupURL=%s, productFamily=%s, lastModified=%s, knownSince=%s, size=%s, stationName=%s, missionId=%s, processingPod=%s, inboxType=%s]",
				this.name, this.relativePath, this.pickupURL, this.productFamily, this.lastModified, this.knownSince,
				this.size, this.stationName, this.missionId, this.processingPod, this.inboxType);
	}

	// --------------------------------------------------------------------------

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getRelativePath() {
		return this.relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}

	public String getPickupURL() {
		return this.pickupURL;
	}

	public void setPickupURL(final String pickupURL) {
		this.pickupURL = pickupURL;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(final Date lastModified) {
		this.lastModified = lastModified;
	}

	public long getSize() {
		return this.size;
	}

	public void setSize(final long size) {
		this.size = size;
	}

	public String getStationName() {
		return this.stationName;
	}

	public void setStationName(final String stationName) {
		this.stationName = stationName;
	}

	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	public String getProcessingPod() { return this.processingPod; }

	public void setProcessingPod(String processingPod) { this.processingPod = processingPod; }

	public String getInboxType() {
		return this.inboxType;
	}

	public void setInboxType(String inboxType) {
		this.inboxType = inboxType;
	}

	public String getProductFamily() {
		return this.productFamily;
	}

	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	public LocalDateTime getKnownSince() {
		return this.knownSince;
	}

	public void setKnownSince(LocalDateTime knownSince) {
		this.knownSince = knownSince;
	}

}
