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

/**
 * Boolean filter for querying the persistence repository.
 */
public class PripBooleanFilter extends PripQueryFilterTerm {

	public enum Function {
		EQ("is"), //
		NE("is not");
		
		private String functionName;
		
		private Function(String functionName) {
			this.functionName = functionName;
		}

		public String getFunctionName() {
			return this.functionName;
		}
		
		public static Function fromString(String function) {
		   for (Function f : Function.values()) {
		      if (f.functionName.equalsIgnoreCase(function) ||
		            f.name().equalsIgnoreCase(function)) {
		         return f;
		      }
			}
			throw new PripFilterOperatorException(String.format("boolean function not supported: %s", function));
		}
	}
	
	// --------------------------------------------------------------------------

	private Function function;
	private Boolean value;
	
	// --------------------------------------------------------------------------

	public PripBooleanFilter(String fieldName) {
		super(fieldName);
	}

	public PripBooleanFilter(String fieldName, Function function, Boolean value) {
		super(fieldName, false, null);
		this.function = Objects.requireNonNull(function);
      this.value = (Objects.requireNonNull(value));
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.function, this.getValue());
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

		final PripBooleanFilter other = (PripBooleanFilter) obj;
		return super.equals(obj) && Objects.equals(this.function, other.function)
				&& Objects.equals(this.getValue(), other.getValue());
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.function ? this.function.functionName : "NO_FUNCTION") + " "
				+ this.getValue();
	}

	public Function getFunction() {
		return this.function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public Boolean getValue() {
		return this.value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

}
