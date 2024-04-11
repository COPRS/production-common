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

package esa.s1pdgs.cpoc.ingestion.trigger.auxip;

import java.util.Date;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auxipState")
public class AuxipState {

	@Id
	private ObjectId id; //necessary for repository.delete(entry)

	private Date nextWindowStart;
	private String processingPod;
	private String pripUrl;
	private String productFamily;

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return "AuxipState{" + "nextWindowStart=" + this.nextWindowStart + ", processingPod='" + this.processingPod
				+ '\'' + ", productFamily='" + this.productFamily + ", pripHost='" + this.pripUrl + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		final AuxipState that = (AuxipState) o;

		return Objects.equals(this.id, that.id) && Objects.equals(this.nextWindowStart, that.nextWindowStart)
				&& Objects.equals(this.processingPod, that.processingPod) && Objects.equals(this.pripUrl, that.pripUrl)
				&& Objects.equals(this.productFamily, that.productFamily);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.nextWindowStart, this.processingPod, this.pripUrl, this.productFamily);
	}

	// --------------------------------------------------------------------------

	public ObjectId getId() {
		return this.id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getProcessingPod() {
		return this.processingPod;
	}

	public void setProcessingPod(String processingPod) {
		this.processingPod = processingPod;
	}

	public String getPripUrl() {
		return this.pripUrl;
	}

	public void setPripUrl(String pripUrl) {
		this.pripUrl = pripUrl;
	}

	public Date getNextWindowStart() {
		return this.nextWindowStart;
	}

	public void setNextWindowStart(Date nextWindowStart) {
		this.nextWindowStart = nextWindowStart;
	}

	public String getProductFamily() {
		return this.productFamily;
	}

	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

}
