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
 * Abstract range value filter for querying the persistence repository.
 */
public abstract class PripRangeValueFilter<T extends Object> extends PripQueryFilterTerm {
	
	private T value;
	private RelationalOperator relationalOperator;
	
	public enum RelationalOperator {
		LT("<"), //
		LE("<="), //
		GT(">"), //
		GE(">="), //
		EQ("="), //
		NE("<>");

		private String op;

		private RelationalOperator(String op) {
			this.op = op;
		}

		public String getOperator() {
			return this.op;
		}

		public static RelationalOperator fromString(String operator) {
			if (null == operator) {
				throw new IllegalArgumentException("operator is required");
			}
			
			if (LT.op.equals(operator) || LT.name().equalsIgnoreCase(operator)) {
				return LT;
			}
			if (LE.op.equals(operator) || LE.name().equalsIgnoreCase(operator)) {
				return LE;
			}
			if (GT.op.equals(operator) || GT.name().equalsIgnoreCase(operator)) {
				return GT;
			}
			if (GE.op.equals(operator) || GE.name().equalsIgnoreCase(operator)) {
				return GE;
			}
			if (EQ.op.equals(operator) || EQ.name().equalsIgnoreCase(operator) || "==".equals(operator)) {
				return EQ;
			}
			if (NE.op.equals(operator) || NE.name().equalsIgnoreCase(operator) || "!=".equals(operator)) {
				return NE;
			}

			throw new PripFilterOperatorException(String.format("operator not supported: %s", operator));
		}
		
		public RelationalOperator getHorizontallyFlippedOperator() {
		   // used for switching operands: x < 3 --> 3 > x
			switch (this) {
			case LT:
				return GT;
			case LE:
				return GE;
			case GT:
				return LT;
			case GE:
				return LE;
			case EQ:
				return EQ;
			case NE:
				return NE;
			default:
				throw new PripFilterOperatorException(String.format("operator not supported: %s", this));
			}
		}
	}

	// --------------------------------------------------------------------------
	
	public PripRangeValueFilter(String fieldName) {
		super(fieldName);
	}

	public PripRangeValueFilter(String fieldName, RelationalOperator operator, T value) {
	   super(fieldName, false, null);
	   this.relationalOperator = Objects.requireNonNull(operator, "relational operator is required!");
      this.value = Objects.requireNonNull(value, "value is required!");
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.value, this.relationalOperator);
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

		final PripRangeValueFilter<?> other = (PripRangeValueFilter<?>) obj;
		return super.equals(obj) && Objects.equals(this.value, other.value)
				&& Objects.equals(this.relationalOperator, other.relationalOperator);
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.relationalOperator ? this.relationalOperator.op : "NO_OP") + " " + this.getValue();
	}

	// --------------------------------------------------------------------------

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public RelationalOperator getRelationalOperator() {
		return this.relationalOperator;
	}

	public void setRelationalOperator(RelationalOperator operator) {
		this.relationalOperator = operator;
	}
	
}
