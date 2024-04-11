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

package de.werum.coprs.nativeapi.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContentDate", type = "object", description = "object containing the start and end dates of the product")
public class ContentDate {

	@JsonProperty("Start")
	@Schema(example = "2021-09-09T18:00:00.000Z", description = "the start date and time of the product", pattern = "YYYY-MM-DDThh:mm:ss.sssZ")
	private String start;

	@JsonProperty("End")
	@Schema(example = "2021-09-09T18:00:00.000Z", description = "the end date and time of the product", pattern = "YYYY-MM-DDThh:mm:ss.sssZ")
	private String end;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getStart() == null) ? 0 : this.getStart().hashCode());
		result = prime * result + ((this.getEnd() == null) ? 0 : this.getEnd().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final ContentDate other = (ContentDate) obj;

		if (this.getStart() == null) {
			if (other.getStart() != null) {
				return false;
			}
		} else if (!this.getStart().equals(other.getStart())) {
			return false;
		}

		if (this.getEnd() == null) {
			if (other.getEnd() != null) {
				return false;
			}
		} else if (!this.getEnd().equals(other.getEnd())) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [start=" + this.getStart() + ", end=" + this.getEnd() + "]";
	}

	public String getStart() {
		return this.start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return this.end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

}
