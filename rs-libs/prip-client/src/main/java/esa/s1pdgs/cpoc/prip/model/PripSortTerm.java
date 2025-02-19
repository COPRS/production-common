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

package esa.s1pdgs.cpoc.prip.model;

import java.util.Objects;

import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;

public class PripSortTerm {

	public enum PripSortOrder {
		ASCENDING("asc"),
		DESCENDING("desc");

		private final String abbreviation;

		private PripSortOrder(String abbreviation) {
			this.abbreviation = abbreviation;
		}

		public String abbreviation() {
			return this.abbreviation;
		}

		public static PripSortOrder fromString(String sortOrder) {
			if (ASCENDING.abbreviation().equalsIgnoreCase(sortOrder) || ASCENDING.name().equalsIgnoreCase(sortOrder)) {
				return ASCENDING;
			}
			if (DESCENDING.abbreviation().equalsIgnoreCase(sortOrder) || DESCENDING.name().equalsIgnoreCase(sortOrder)) {
				return DESCENDING;
			}

			throw new IllegalArgumentException(String.format("sort order not supported: %s", sortOrder));
		}
	}

	// --------------------------------------------------------------------------

	private FIELD_NAMES sortFieldName;
	private PripSortOrder sortOrder;

	// --------------------------------------------------------------------------

	public PripSortTerm(FIELD_NAMES sortFieldName, PripSortOrder sortOrder) {
		this.sortFieldName = sortFieldName;
		this.sortOrder = sortOrder;
	}

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return this.sortFieldName + " " + (null != this.sortOrder ? this.sortOrder.abbreviation() : "NO_SORT_ORDER");
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.sortFieldName, this.sortOrder);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}

		final PripSortTerm other = (PripSortTerm) obj;
		return Objects.equals(this.sortFieldName, other.sortFieldName)
				&& Objects.equals(this.sortOrder, other.sortOrder);
	}

	// --------------------------------------------------------------------------

	public PripSortOrder getSortOrderOrDefault(PripSortOrder defaultSortOrder) {
		return (null != this.sortOrder ? this.sortOrder : defaultSortOrder);
	}

	public PripSortOrder getSortOrder() {
		return this.sortOrder;
	}

	public void setSortOrder(PripSortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	public FIELD_NAMES getSortFieldName() {
		return this.sortFieldName;
	}

	public void setSortFieldName(FIELD_NAMES sortFieldName) {
		this.sortFieldName = sortFieldName;
	}

}
