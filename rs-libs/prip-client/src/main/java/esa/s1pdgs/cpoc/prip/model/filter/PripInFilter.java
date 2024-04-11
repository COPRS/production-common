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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class PripInFilter extends PripQueryFilterTerm {
	
	public static final String FIELD_TYPE_STRING = "string"; 

	public enum Function {
		IN("in");

		private String functionName;

		private Function(String functionName) {
			this.functionName = functionName;
		}

		public String getFunctionName() {
			return this.functionName;
		}

		public static Function fromString(String function) {
			for (Function f : Function.values()) {
				if (f.functionName.equalsIgnoreCase(function) || f.name().equalsIgnoreCase(function)) {
					return f;
				}
			}
			throw new PripFilterOperatorException(String.format("terms filter function not supported: %s", function));
		}

	}
	
	private Function function;
	private List<Object> terms;

	public PripInFilter(String fieldName) {
		super(fieldName);
	}

	public PripInFilter(PripMetadata.FIELD_NAMES fieldName) {
		this(fieldName.fieldName());
	}
	
	public PripInFilter(String fieldName, Function function, List<Object> terms) {
		super(fieldName, false, null);
		this.function = Objects.requireNonNull(function);
		this.terms = Objects.requireNonNull(terms);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.function, this.terms);
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

		final PripInFilter other = (PripInFilter) obj;
		return super.equals(obj) && Objects.equals(this.function, other.function)
				&& Objects.equals(this.terms, other.terms);
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.function ? this.function.name() : "NO_FUNCTION") + " "
				+ this.terms;
	}
	
	public Function getFunction() {
		return this.function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}
	
	public List<Object> getTerms() {
		return this.terms;
	}
	
	public List<Object> getTermsInLowerCase() {
		if (getFieldName().endsWith("_" + FIELD_TYPE_STRING)) {

			List<Object> listObjects = new ArrayList<>();
			for (Object o : terms) {
				listObjects.add(((String) o).toLowerCase());
			}
			return listObjects;

		} else {
			return this.terms;
		}
	}
	
	public void setTerms(List<Object> terms) {
		this.terms = terms;
	}

}
