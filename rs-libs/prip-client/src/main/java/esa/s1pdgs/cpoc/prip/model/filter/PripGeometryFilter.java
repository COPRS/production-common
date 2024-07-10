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

package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

import org.locationtech.jts.geom.Geometry;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;

/**
 * Geometry filter for querying the persistence repository.
 */
public class PripGeometryFilter extends PripQueryFilterTerm {

	public enum Function {
		INTERSECTS("intersects"), //
		DISJOINTS("disjoints"), //
		WITHIN("within"); //
		
		private String functionName;
		
		private Function(String functionName) {
			this.functionName = functionName;
		}

		public String getFunctionName() {
			return this.functionName;
		}
		
		public static Function fromString(String function) {
			if (INTERSECTS.functionName.equalsIgnoreCase(function) || INTERSECTS.name().equalsIgnoreCase(function)) {
				return INTERSECTS;
			}
			if (DISJOINTS.functionName.equalsIgnoreCase(function) || DISJOINTS.name().equalsIgnoreCase(function)) {
				return DISJOINTS;
			}
			if (WITHIN.functionName.equalsIgnoreCase(function) || WITHIN.name().equalsIgnoreCase(function)) {
				return WITHIN;
			}

			throw new PripFilterOperatorException(String.format("polygon filter function not supported: %s", function));
		}
	}
	
	// --------------------------------------------------------------------------

	private Function function;
	private Geometry geometry;
	
	// --------------------------------------------------------------------------

	public PripGeometryFilter(String fieldName) {
		super(fieldName);
	}
	
	public PripGeometryFilter(PripMetadata.FIELD_NAMES fieldName) {
		this(fieldName.fieldName());
	}

	public PripGeometryFilter(String fieldName, Function function, Geometry geometry) {
	   super(fieldName, false, null);
	   this.function = Objects.requireNonNull(function);
	   this.geometry = geometry;
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.function, this.geometry);
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

		final PripGeometryFilter other = (PripGeometryFilter) obj;
		return super.equals(obj) && Objects.equals(this.function, other.function)
				&& Objects.equals(this.geometry, other.geometry);
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.function ? this.function.name() : "NO_FUNCTION") + " "
				+ this.geometry;
	}

	public Function getFunction() {
		return this.function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public Geometry getGeometry() {
		return this.geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

}
