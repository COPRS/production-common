package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

/**
 * Abstract range value filter for querying the persistence repository.
 */
public abstract class PripRangeValueFilter<T extends Object> extends PripQueryFilter {
	
	private T value;
	private Operator operator;
	
	public enum Operator {
		LT("<"), //
		LE("<="), //
		GT(">"), //
		GE(">="), //
		EQ("="), //
		NE("<>");

		private String op;

		private Operator(String op) {
			this.op = op;
		}

		public String getOperator() {
			return op;
		}

		public Operator fromString(String operator) {
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

			throw new IllegalArgumentException(String.format("operator not supported: %s", operator));
		}
	}

	// --------------------------------------------------------------------------
	
	public PripRangeValueFilter(String fieldName) {
		super(fieldName);
	}
	
	public PripRangeValueFilter(String fieldName, Operator operator, T value) {
		this(fieldName);

		this.operator = Objects.requireNonNull(operator, "operator is required!");
		this.value = Objects.requireNonNull(value, "value is required!");
	}
	
	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.value, this.operator);
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
				&& Objects.equals(this.operator, other.operator);
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.operator ? this.operator.op : "NO_OP") + " " + this.getValue();
	}

	// --------------------------------------------------------------------------

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Operator getOperator() {
		return this.operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
}
