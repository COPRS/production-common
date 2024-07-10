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

package esa.s1pdgs.cpoc.appstatus.dto;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * @author Viveris Technologies
 */
public class StatusPerCategoryDto {

	/**
	 * Status
	 */
	private AppState status;

	/**
	 * Number of milliseconds passed for the last modification
	 */
	private long timeSinceLastChange;

	/**
	 * Number of error for the last modification of status
	 */
	private int errorCounter;

	/**
	 * Category
	 */
	private ProductCategory category;

	public StatusPerCategoryDto() {
		timeSinceLastChange = 0;
		errorCounter = 0;
	}

	/**
	 * @param state
	 * @param lastChange
	 * @param errorCounter
	 */
	public StatusPerCategoryDto(final AppState state, final long timeLastChange, final int errorCounter,
			final ProductCategory category) {
		super();
		this.status = state;
		this.timeSinceLastChange = timeLastChange;
		this.errorCounter = errorCounter;
		this.category = category;
	}

	/**
	 * @return the status
	 */
	public AppState getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(final AppState status) {
		this.status = status;
	}

	/**
	 * @return the timeSinceLastChange
	 */
	public long getTimeSinceLastChange() {
		return timeSinceLastChange;
	}

	/**
	 * @param timeLastChange
	 *            the timeLastChange to set
	 */
	public void setTimeSinceLastChange(final long timeLastChange) {
		this.timeSinceLastChange = timeLastChange;
	}

	/**
	 * @return the errorCounter
	 */
	public int getErrorCounter() {
		return errorCounter;
	}

	/**
	 * @param errorCounter
	 *            the errorCounter to set
	 */
	public void setErrorCounter(final int errorCounter) {
		this.errorCounter = errorCounter;
	}

	/**
	 * @return the category
	 */
	public ProductCategory getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(final ProductCategory category) {
		this.category = category;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{status: %s, timeSinceLastChange: %d, errorCounter: %d, category: %s}", status,
				timeSinceLastChange, errorCounter, category);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(status, timeSinceLastChange, errorCounter, category);
	}

	/**
	 * @see java.lang.Object#equals()
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			StatusPerCategoryDto other = (StatusPerCategoryDto) obj;
			// field comparison
			ret = Objects.equals(status, other.status) && timeSinceLastChange == other.timeSinceLastChange
					&& errorCounter == other.errorCounter && category == other.category;
		}
		return ret;
	}

}
