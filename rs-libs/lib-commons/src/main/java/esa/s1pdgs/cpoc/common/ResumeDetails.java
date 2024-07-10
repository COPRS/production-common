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

package esa.s1pdgs.cpoc.common;

import java.util.Objects;

/**
 * Details to resume an error
 * @author Cyrielle Gailliard
 *
 */
public class ResumeDetails {
	
	/**
	 * Topic name
	 */
	private final String topicName;
	
	/**
	 * DTO
	 */
	private final Object dto;
	
	/**
	 * Constructor
	 * @param topicName
	 * @param dto
	 */
	public ResumeDetails(final String topicName, final Object dto) {
		this.topicName = topicName;
		this.dto = dto;
	}

	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * @return the dto
	 */
	public Object getDto() {
		return dto;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{topicName: %s, dto: %s}", topicName, dto);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(topicName, dto);
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
			ResumeDetails other = (ResumeDetails) obj;
			ret = Objects.equals(topicName, other.topicName) && Objects.equals(dto, other.dto);
		}
		return ret;
	}
	
	
}
