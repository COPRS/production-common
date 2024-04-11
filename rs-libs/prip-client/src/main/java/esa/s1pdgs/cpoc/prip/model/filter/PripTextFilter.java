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

import esa.s1pdgs.cpoc.prip.model.PripMetadata;

/**
 * Text filter for querying the persistence repository.
 */
public class PripTextFilter extends PripQueryFilterTerm {

	public enum Function {
		STARTS_WITH("startswith"), //
		ENDS_WITH("endswith"), //
		CONTAINS("contains"), //
		EQ("eq"), //
	   NE("ne");
		
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
			throw new PripFilterOperatorException(String.format("text filter function not supported: %s", function));
		}
	}
	
	// --------------------------------------------------------------------------

	private Function function;
	private String text;
	
	// --------------------------------------------------------------------------

	public PripTextFilter(String fieldName) {
		super(fieldName);
	}
	
	public PripTextFilter(PripMetadata.FIELD_NAMES fieldName) {
		this(fieldName.fieldName());
	}

	public PripTextFilter(String fieldName, Function function, String text) {
		super(fieldName, false, null);
		this.function = Objects.requireNonNull(function);
      this.text = Objects.requireNonNull(text);
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.function, this.text);
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

		final PripTextFilter other = (PripTextFilter) obj;
		return super.equals(obj) && Objects.equals(this.function, other.function)
				&& Objects.equals(this.text, other.text);
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.function ? this.function.name() : "NO_FUNCTION") + " "
				+ this.text;
	}

	public Function getFunction() {
		return this.function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
